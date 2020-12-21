plugins {
    id("java-library")
}

group = "net.flintmc"

flint {
    minecraftVersions("1.15.2")
}

dependencies {

    minecraft("1.15.2") {
        annotationProcessor(project(":annotation-processing:annotation-processing-autoload"))
    }

    annotationProcessor(project(":annotation-processing:annotation-processing-autoload"))
    internalAnnotationProcessor(project(":annotation-processing:annotation-processing-autoload"))

    api(project(":framework:framework-eventbus"))
    api(project(":framework:framework-inject"))
    api(project(":framework:framework-stereotype"))
    api(project(":framework:framework-config"))

    api(project(":transform:transform-hook"))
    api(project(":transform:transform-shadow"))
    api(project(":render:render-model-renderer"))
    api(project(":render:render-model-renderer", "internal"))

    api(project(":render:render-gui"))

    api(project(":util:util-i18n"))

    api("com.google.code.gson", "gson", "2.8.6")
}
