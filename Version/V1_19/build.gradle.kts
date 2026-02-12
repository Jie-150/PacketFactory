taboolib{
    subproject = true
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v11903:11903:mapped")
    compileOnly(project(":Common"))
}

gradle.buildFinished {
    layout.buildDirectory.get().asFile.deleteRecursively()
}