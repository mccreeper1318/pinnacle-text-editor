plugins {
    application
}

group = "org.pinnacle"
version = "0.2.2"

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
        attributes["Implementation-Version"] = project.version.toString()
    }
}

val updateRepository = providers.gradleProperty("updateRepository")
    .orElse("mccreeper1318/pinnacle-text-editor")

val prepareJpackageInput by tasks.registering(Sync::class) {
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
            "--app-version", project.version.toString(),
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
            "--java-options", "-Dpinnacle.update.repository=${updateRepository.get()}"
        )
    }

    doLast {
        val installer = layout.buildDirectory.dir("jpackage/dist").get().asFile
            .listFiles()
            ?.firstOrNull { it.extension == "deb" }
            ?: error("jpackage did not create a .deb installer")

        val process = ProcessBuilder(
            "bash",
            file("packaging/linux/fix-deb-dependencies.sh").absolutePath,
            installer.absolutePath
        ).inheritIO().start()

        check(process.waitFor() == 0) {
            "Failed to make the Debian package dependencies portable."
        }
    }
}
