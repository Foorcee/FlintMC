plugins {
    id("net.flintmc.flint-gradle-plugin")
    id("project-report")
}

fun RepositoryHandler.labymedia() {
    val labymediaMavenAuthToken: String? by project

    maven {
        setUrl("https://git.laby.tech/api/v4/groups/2/-/packages/maven")
        name = "Gitlab"
        credentials(HttpHeaderCredentials::class) {
            if (System.getenv().containsKey("CI_JOB_TOKEN")) {
                name = "Job-Token"
                value = System.getenv("CI_JOB_TOKEN")
            } else {
                name = "Private-Token"
                value = labymediaMavenAuthToken
            }
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }
}

repositories {
    labymedia()
    mavenCentral()
}


subprojects {
    plugins.withId("java") {
        version = System.getenv().getOrDefault("VERSION", "1.0.0")
        repositories {
            labymedia()
            mavenCentral()
        }
    }
}

allprojects {
    configurations.all {
        resolutionStrategy {
            force("org.apache.logging.log4j:log4j-api:2.8.2")
            force("com.google.guava:guava:27.0.1-jre")
            force("org.apache.commons:commons-lang3:3.10")
            force("org.apache.logging.log4j:log4j-core:2.8.2")
            force("it.unimi.dsi:fastutil:8.2.1")
            force("net.java.dev.jna:jna:4.4.0")
            force("com.google.code.findbugs:jsr305:3.0.2")
            force("com.google.code.gson:gson:2.8.6")
            force("commons-io:commons-io:2.6")
            force("commons-codec:commons-codec:1.10")
            force("com.beust:jcommander:1.78")
        }
    }
}

flint {
    flintVersion = System.getenv().getOrDefault("VERSION", "1.0.0")

    projectFilter {
        !arrayOf(":", ":framework", ":render", ":transform", ":util").contains(it.path)
    }

    authors = arrayOf("LabyMedia GmbH")

    type = net.flintmc.gradle.extension.FlintGradleExtension.Type.LIBRARY

    runs {
        overrideMainClass("net.flintmc.launcher.FlintLauncher")
    }
}