import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    java
    application
    // Apply the Checkstyle plugin
    checkstyle
    // Apply the PMD plugin
    pmd
    // Apply the Spotless plugin
    id("com.diffplug.spotless") version "6.25.0"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

// Spotless configuration
spotless {
    // Configure Java formatting
    java {
        // Use google-java-format, which follows Google Style Guide
        googleJavaFormat()
        // Remove unused imports
        removeUnusedImports()
        // Ensure consistent line endings
        lineEndings = com.diffplug.spotless.LineEnding.PLATFORM_NATIVE // Or WINDOWS, UNIX
    }
    // Optional: Configure formatting for other file types like build.gradle.kts itself
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("1.2.1")
    }
}

// Define e2eTest SourceSet FIRST
sourceSets {
    create("e2eTest") {
        java.srcDirs("src/e2eTest/java")
        resources.srcDirs("src/e2eTest/resources")
        // Reverted: Classpaths are configured via configurations and dependencies below
        // compileClasspath = sourceSets.main.get().output + configurations.getByName("e2eTestImplementation") // Reverted
        // runtimeClasspath = output + compileClasspath + configurations.getByName("e2eTestRuntimeOnly") // Reverted
    }
}

// Configure the implicitly created e2eTest configurations SECOND
configurations {
    getByName("e2eTestImplementation") {
        extendsFrom(configurations.testImplementation.get())
    }
    getByName("e2eTestRuntimeOnly") {
        extendsFrom(configurations.testRuntimeOnly.get()) // Keep this
    }
}

// Define dependencies THIRD
dependencies {
    // Add your project dependencies here

    // --- Remove original direct JUnit dependencies ---
    // testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2") // Will be managed by BOM
    // testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2") // Will be managed by BOM

    // --- Add JUnit BOM and dependencies for standard tests ---
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Mocking for tests
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")

    // CLI Argument Parsing
    implementation("info.picocli:picocli:4.7.6")

    // Jakarta Bean Validation API
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    // Hibernate Validator (Implementation for Validation API)
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    // Required for Hibernate Validator
    runtimeOnly("org.glassfish:jakarta.el:4.0.2")

    // Logging Facade (SLF4j)
    implementation("org.slf4j:slf4j-api:2.0.12") // Use a recent 2.x version
    // Logging Implementation (Logback)
    runtimeOnly("ch.qos.logback:logback-classic:1.4.14") // Added for runtime
    testImplementation("ch.qos.logback:logback-classic:1.4.14") // Kept for ListAppender in tests

    // E2E Test Dependencies
    // Explicitly depend on main and test outputs for E2E compile time
    "e2eTestImplementation"(sourceSets.main.get().output)
    "e2eTestImplementation"(sourceSets.test.get().output)

    // ---> KEEP these explicit RuntimeOnly dependencies for e2eTest <---
    "e2eTestRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine") // KEEP
    "e2eTestRuntimeOnly"("org.junit.platform:junit-platform-launcher") // KEEP

    // Runtime dependencies should be covered by extendsFrom in the configurations block - This comment might now be less relevant as we explicitly declare them.
}

application {
    // Updated main class name based on previous steps
    mainClass.set("com.spantry.SpantryApplication")
}

// Configure the default JAR task to include the Main-Class and Class-Path manifest attributes
tasks.jar {
    manifest {
        attributes(
            "Main-Class" to application.mainClass.get(),
            // Add runtime classpath to manifest. Relative paths assume JAR and libs are in the same dir.
            // If JAR is in build/libs, paths should be relative to that.
            "Class-Path" to configurations.runtimeClasspath.get().files.joinToString(" ") { "../libs/${it.name}" }, // Adjust path if needed
            // A simpler approach if JAR and libs ARE in the same dir (e.g., in a distribution):
            // "Class-Path" to configurations.runtimeClasspath.get().files.joinToString(" ") { it.name }
        )
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed", "standard_out", "standard_error")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
        showStackTraces = true
        showCauses = true
    }
}

// Checkstyle configuration
checkstyle {
    toolVersion = "10.14.2"
    configFile =
        resources.text.fromUri(
            "https://raw.githubusercontent.com/checkstyle/checkstyle/checkstyle-10.14.2/src/main/resources/google_checks.xml",
        ).asFile()
    configProperties["checkstyle.cache.file"] = "$buildDir/checkstyle.cache"
    isIgnoreFailures = false
    isShowViolations = true
}

// PMD configuration
pmd {
    toolVersion = "7.1.0"
    ruleSets = emptyList()
    ruleSetFiles = files("$rootDir/config/pmd/ruleset.xml")
    isIgnoreFailures = false
}

tasks.named<Pmd>("pmdE2eTest") {
    ruleSets = emptyList()
    ruleSetFiles = files("$rootDir/config/pmd/ruleset.xml")
}

// Define e2eTest task
tasks.register<Test>("e2eTest") {
    description = "Runs end-to-end tests."
    group = "verification"

    testClassesDirs = sourceSets["e2eTest"].output.classesDirs
    classpath = sourceSets["e2eTest"].runtimeClasspath
    useJUnitPlatform()

    // Ensure application distribution is installed before running E2E tests
    dependsOn(tasks.installDist)

    testLogging {
        events("passed", "skipped", "failed")
    }

    // Ensure E2E tests always run
    outputs.upToDateWhen { false }

    shouldRunAfter(tasks.test)
}

// Configure handling for duplicate resources in e2eTest
tasks.named<Copy>("processE2eTestResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

// Ensure the check task depends on static analysis AND e2eTest
tasks.named("check") {
    dependsOn(tasks.withType<Checkstyle>())
    dependsOn(tasks.withType<Pmd>())
    dependsOn(tasks.named("spotlessCheck"))
    dependsOn(tasks.named("e2eTest"))
}

// Optional: Make spotlessApply run before compilation to auto-format
tasks.withType<JavaCompile>().configureEach {
    dependsOn(tasks.named("spotlessApply"))
}

// Explicitly configure compile task dependency for e2eTest
tasks.named<JavaCompile>("compileE2eTestJava") {
    dependsOn(tasks.named("testClasses"))
    // Explicitly set the classpath for this compile task
    classpath = sourceSets["e2eTest"].compileClasspath
} 
