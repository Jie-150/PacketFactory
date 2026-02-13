taboolib{
   subproject = true
}

dependencies {
   compileOnly("ink.ptms.core:v11701:11701:mapped")
   // mapped未提供CraftParticle需要引用universal
   compileOnly("ink.ptms.core:v11701:11701:universal")
   compileOnly("ink.ptms.core:v11605:11605")
   compileOnly(project(":Common"))
}

gradle.buildFinished {
   layout.buildDirectory.get().asFile.deleteRecursively()
}