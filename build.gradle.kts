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
        // Classpaths are handled via configurations and dependencies block
    }
}

// Configure the implicitly created e2eTest configurations SECOND
configurations {
    getByName("e2eTestImplementation") {
        extendsFrom(configurations.testImplementation.get())
    }
    getByName("e2eTestRuntimeOnly") {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
}

// Define dependencies THIRD
dependencies {
    // Add your project dependencies here
    // Example: testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    // testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")

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
    // Logging Implementation (Logback) - Needed for compiling test code (ListAppender)
    testImplementation("ch.qos.logback:logback-classic:1.4.14") // Corresponds to slf4j-api 2.x

    // E2E Test Dependencies will be inherited via configurations
    // Add dependency on test source set output for E2E tests (using string notation)
    "e2eTestImplementation"(sourceSets.test.get().output)
}

application {
    // Updated main class name based on previous steps
    mainClass.set("com.spantry.SpantryApplication")
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
    ruleSets =
        listOf(
            "category/java/bestpractices.xml",
            "category/java/codestyle.xml",
            "category/java/design.xml",
            "category/java/errorprone.xml",
            "category/java/multithreading.xml",
            "category/java/performance.xml",
            "category/java/security.xml",
        )
    isIgnoreFailures = false
}

// Define e2eTest task
tasks.register<Test>("e2eTest") {
    description = "Runs end-to-end tests."
    group = "verification"

    testClassesDirs = sourceSets["e2eTest"].output.classesDirs
    classpath = configurations.getByName("e2eTestRuntimeClasspath") + sourceSets["e2eTest"].output
    useJUnitPlatform()

    // Ensure application JAR is built before running E2E tests
    dependsOn(tasks.jar)

    testLogging {
        events("passed", "skipped", "failed")
    }

    // Ensure E2E tests always run
    outputs.upToDateWhen { false }

    shouldRunAfter(tasks.test)
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
