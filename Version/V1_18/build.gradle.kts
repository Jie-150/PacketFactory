taboolib{
    subproject = true
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v11802:11802:mapped")
    compileOnly("ink.ptms.core:v11802:11802:universal")
    compileOnly(project(":Common"))
}

gradle.buildFinished {
    layout.buildDirectory.get().asFile.deleteRecursively()
}