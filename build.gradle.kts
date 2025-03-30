import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    java
    application
    // Apply the Checkstyle plugin
    checkstyle
    // Apply the PMD plugin
    pmd
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

dependencies {
    // Add your project dependencies here
    // Example: testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    // testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")

    // CLI Argument Parsing
    implementation("info.picocli:picocli:4.7.6")
}

application {
    mainClass.set("com.spantry.Main") // Replace with your actual main class
}

tasks.test {
    useJUnitPlatform()
}

// Checkstyle configuration
checkstyle {
    // Use a recent Checkstyle version
    toolVersion = "10.14.2"
    // Use Google's Checkstyle configuration as a base (will download if needed)
    // You can later create a custom config file like 'config/checkstyle/checkstyle.xml'
    // configFile = file("config/checkstyle/checkstyle.xml")
    configProperties["checkstyle.cache.file"] = "${buildDir}/checkstyle.cache"
    isIgnoreFailures = false // Fail build on violations
    isShowViolations = true
}

// PMD configuration
pmd {
    // Use a recent PMD version
    toolVersion = "7.1.0"
    // Define the rule sets to use (using built-in PMD rules)
    ruleSets = listOf(
        "category/java/bestpractices.xml",
        "category/java/codestyle.xml",
        "category/java/design.xml",
        "category/java/errorprone.xml",
        "category/java/multithreading.xml",
        "category/java/performance.xml",
        "category/java/security.xml"
    )
    isIgnoreFailures = false // Fail build on violations
}

// Ensure the check task depends on checkstyle and pmd tasks
tasks.named("check") { 
    dependsOn(tasks.withType<Checkstyle>()) 
    dependsOn(tasks.withType<Pmd>()) 
} 