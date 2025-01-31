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
    id("java-library")
}

group = "net.flintmc"



dependencies {
    minecraft("1.15.2", "1.16.5") {
        annotationProcessor(project(":annotation-processing:annotation-processing-autoload"))
    }
    annotationProcessor(project(":annotation-processing:annotation-processing-autoload"))
    internalAnnotationProcessor(project(":annotation-processing:annotation-processing-autoload"))

    api("net.flintmc.launcher", "flint-launcher", "1.0.2")

    api(project(":framework:framework-inject"))
    api(project(":util:util-csv"))

    api("org.ow2.asm", "asm", "7.2-beta")
    api("org.javassist", "javassist", "3.27.0-GA")
    api("com.google.code.gson", "gson", "2.8.6")

}
