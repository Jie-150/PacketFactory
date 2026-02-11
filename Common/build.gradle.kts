taboolib{
    subproject = true
}

dependencies {
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v11605:11605")
}

gradle.buildFinished {
    buildDir.deleteRecursively()
}