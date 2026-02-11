taboolib{
    subproject = true
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v12000:12000:mapped")
    compileOnly(project(":Common"))
}

gradle.buildFinished {
    buildDir.deleteRecursively()
}