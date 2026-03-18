taboolib{
    subproject = true
}

dependencies {
    compileOnly(project(":Common"))
    compileOnly("ink.ptms.core:v12104:12104:mapped")
    compileOnly("ink.ptms:nms-all:1.0.0")
}

gradle.buildFinished {
    layout.buildDirectory.get().asFile.deleteRecursively()
}