taboolib{
   subproject = true
}

dependencies {
   compileOnly("ink.ptms.core:v11701:11701:mapped")
   compileOnly("ink.ptms.core:v11801:11801:mapped")
   compileOnly(project(":Common"))
}

gradle.buildFinished {
   buildDir.deleteRecursively()
}