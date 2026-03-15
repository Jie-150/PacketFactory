taboolib{
    subproject = true
}

dependencies {
    compileOnly("ink.ptms.core:v12103:12103:mapped")
    compileOnly("com.mojang:authlib:7.1.61")
    compileOnly("io.netty:netty-all:4.1.86.Final")
    compileOnly("com.mojang:brigadier:1.0.500")
    compileOnly("it.unimi.dsi:fastutil:8.5.18")
    compileOnly(project(":Common"))
}

gradle.buildFinished {
    layout.buildDirectory.get().asFile.deleteRecursively()
}