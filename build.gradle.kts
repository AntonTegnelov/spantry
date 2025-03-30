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
}

application {
    mainClass.set("com.spantry.Main") // Replace with your actual main class
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
    // Use a recent Checkstyle version
    toolVersion = "10.14.2"
    // Use Google's Checkstyle configuration as a base (will download if needed)
    // You can later create a custom config file like 'config/checkstyle/checkstyle.xml'
    configFile =
        resources.text.fromUri(
            "https://raw.githubusercontent.com/checkstyle/checkstyle/checkstyle-10.14.2/src/main/resources/google_checks.xml",
        ).asFile()
    configProperties["checkstyle.cache.file"] = "$buildDir/checkstyle.cache"
    isIgnoreFailures = false // Fail build on violations
    isShowViolations = true
}

// PMD configuration
pmd {
    // Use a recent PMD version
    toolVersion = "7.1.0"
    // Define the rule sets to use (using built-in PMD rules)
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
    isIgnoreFailures = false // Fail build on violations
}

// Ensure the check task depends on checkstyle and pmd tasks
// AND make check task depend on spotlessCheck for verification
tasks.named("check") {
    dependsOn(tasks.withType<Checkstyle>())
    dependsOn(tasks.withType<Pmd>())
    dependsOn(tasks.named("spotlessCheck")) // Add dependency on spotlessCheck
}

// Optional: Make spotlessApply run before compilation to auto-format
tasks.withType<JavaCompile>().configureEach {
    dependsOn(tasks.named("spotlessApply"))
} 
