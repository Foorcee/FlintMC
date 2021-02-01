/*
 * FlintMC
 * Copyright (C) 2020-2021 LabyMedia GmbH and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

plugins {
    id("net.flintmc.flint-gradle")
    id("net.minecrell.licenser") version "0.4.1"
}

fun RepositoryHandler.flintRepository() {
    maven {
        setUrl("https://dist.labymod.net/api/v1/maven/release")
        name = "Flint"
    }
}

repositories {
    mavenLocal()
    flintRepository()
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

subprojects {

    plugins.withId("java") {
        apply<MavenPublishPlugin>()
        plugins.apply("net.minecrell.licenser")

        version = System.getenv().getOrDefault("VERSION", "1.0.0")

        repositories {
            flintRepository()
            mavenCentral()
        }

        tasks.withType<JavaCompile> {
            options.isFork = true
        }

        tasks.test {
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
            }
        }

        license {
            header = rootProject.file("LICENSE-HEADER")
            include("**/*.java")
            include("**/*.kts")

            tasks {
                create("gradle") {
                    files = project.files("build.gradle.kts", "settings.gradle.kts")
                }
            }
        }
    }
}

flint {
    flintVersion = System.getenv().getOrDefault("VERSION", "1.0.0")

    projectFilter {
        !arrayOf(":", ":framework", ":render", ":transform", ":util", ":minecraft").contains(it.path)
    }

    minecraftVersions("1.15.2", "1.16.5")

    type = net.flintmc.gradle.extension.FlintGradleExtension.Type.LIBRARY
    authors = arrayOf("LabyMedia GmbH")

    runs {
        overrideMainClass("net.flintmc.launcher.FlintLauncher")
    }
}
