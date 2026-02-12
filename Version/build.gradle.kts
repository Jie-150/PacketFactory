taboolib {
    subproject = true
}

gradle.buildFinished {
    layout.buildDirectory.get().asFile.deleteRecursively()
}