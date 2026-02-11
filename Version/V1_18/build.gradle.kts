taboolib{
    subproject = true
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v11802:11802:mapped")
    compileOnly(project(":Common"))
}

gradle.buildFinished {
    buildDir.deleteRecursively()
}