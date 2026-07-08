taboolib{
    subproject = true
}

dependencies {
    compileOnly(project(":Common"))
    compileOnly("ink.ptms.core:v12111:12111:mapped")
    compileOnly("net.md-5:bungeecord-chat:1.21-R0.4")
    compileOnly("ink.ptms:nms-all:1.0.0")
}

gradle.buildFinished {
    layout.buildDirectory.get().asFile.deleteRecursively()
}