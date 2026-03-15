taboolib{
    subproject = true
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v12000:12000:mapped")
    compileOnly("it.unimi.dsi:fastutil:8.5.18")
    compileOnly(project(":Common"))
}

gradle.buildFinished {
    layout.buildDirectory.get().asFile.deleteRecursively()
}