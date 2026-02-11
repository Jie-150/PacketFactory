taboolib{
    subproject = true
}

dependencies {
    compileOnly("ink.ptms.core:v12101:12101:mapped")
    compileOnly(project(":Common"))
}

gradle.buildFinished {
    buildDir.deleteRecursively()
}