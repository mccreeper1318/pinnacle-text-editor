import org.gradle.api.tasks.testing.Test

plugins {
    application
}

group = "org.pinnacle"

val appVersion = providers.gradleProperty("appVersion")
    .orElse("0.3")
    .get()
val jpackageVersion = appVersion.substringBefore("-")

version = appVersion

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "org.pinnacle.texteditor.PinnacleTextEditor"
    applicationName = "pinnacle-text-editor"
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 21
}

tasks.withType<Jar>().configureEach {
    archiveBaseName = "PinnacleTextEditor"
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
        attributes["Implementation-Title"] = "Pinnacle Text Editor"
        attributes["Implementation-Version"] = appVersion
    }
}

val selfTest = tasks.register<JavaExec>("selfTest") {
    group = "verification"
    description = "Runs dependency-free regression checks for version parsing, the Esc menu, and print pagination."
    dependsOn(tasks.named("testClasses"))
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("org.pinnacle.texteditor.CoreBehaviorSelfTest")
    jvmArgs("-Djava.awt.headless=true")
}

tasks.named<Test>("test") {
    // These test-source files are dependency-free self-test programs with main methods,
    // rather than JUnit or TestNG classes. The selfTest task executes them directly.
    // Gradle 9 otherwise fails because source files exist but no framework tests are found.
    failOnNoDiscoveredTests.set(false)
}

tasks.named("check") {
    dependsOn(selfTest)
}

val updateRepository = providers.gradleProperty("updateRepository")
    .orElse("mccreeper1318/pinnacle-text-editor")

val prepareJpackageInput = tasks.register<Sync>("prepareJpackageInput") {
    dependsOn(tasks.jar)
    from(tasks.jar.flatMap { it.archiveFile })
    into(layout.buildDirectory.dir("jpackage/input"))
}

tasks.register<Exec>("packageDeb") {
    group = "distribution"
    description = "Builds a self-contained Linux .deb installer with an application-menu shortcut."
    dependsOn(prepareJpackageInput)

    doFirst {
        val output = layout.buildDirectory.dir("jpackage/dist").get().asFile
        output.mkdirs()
        output.listFiles()?.forEach { it.deleteRecursively() }

        commandLine(
            "jpackage",
            "--type", "deb",
            "--name", "Pinnacle Text Editor",
            "--linux-package-name", "pinnacle-text-editor",
            "--app-version", jpackageVersion,
            "--vendor", "Pinnacle",
            "--description", "A fullscreen, distraction-free plain-text editor.",
            "--input", layout.buildDirectory.dir("jpackage/input").get().asFile.absolutePath,
            "--main-jar", tasks.jar.get().archiveFileName.get(),
            "--main-class", application.mainClass.get(),
            "--dest", output.absolutePath,
            "--icon", file("src/main/resources/pinnacle-text-editor.png").absolutePath,
            "--linux-shortcut",
            "--linux-menu-group", "Office",
            "--linux-app-category", "editors",
            "--file-associations", file("packaging/linux/txt-association.properties").absolutePath,
            "--install-dir", "/opt",
            "--license-file", file("LICENSE").absolutePath,
            "--add-modules", "java.base,java.desktop,java.net.http,java.prefs",
            "--java-options", "-Dpinnacle.packaged=true",
            "--java-options", "-Dpinnacle.app.version=$appVersion",
            "--java-options", "-Dpinnacle.update.repository=${updateRepository.get()}"
        )
    }

    doLast {
        val installer = layout.buildDirectory.dir("jpackage/dist").get().asFile
            .listFiles()
            ?.singleOrNull { it.extension == "deb" }
            ?: error("jpackage did not create exactly one .deb installer")

        val process = ProcessBuilder(
            "bash",
            file("packaging/linux/fix-deb-dependencies.sh").absolutePath,
            installer.absolutePath,
            appVersion
        ).inheritIO().start()

        check(process.waitFor() == 0) {
            "Failed to finalize the Debian package."
        }
    }
}
