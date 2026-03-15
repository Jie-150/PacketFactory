taboolib{
    subproject = true
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v11802:11802:mapped")
    compileOnly("ink.ptms.core:v11802:11802:universal")
    compileOnly("com.mojang:authlib:7.1.61")
    compileOnly("it.unimi.dsi:fastutil:8.5.18")
    compileOnly("com.mojang:brigadier:1.0.500")
    compileOnly(project(":Common"))
}

gradle.buildFinished {
    layout.buildDirectory.get().asFile.deleteRecursively()
}