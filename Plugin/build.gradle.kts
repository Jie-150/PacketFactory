plugins {
    `maven-publish`
}

taboolib {
    description {
        name(rootProject.name)
    }
    subproject = true
}

dependencies {
}

tasks {
    jar {
        // 构件名
        archiveBaseName.set(rootProject.name)
        // 打包子项目源代码
        rootProject.subprojects.forEach { from(it.sourceSets["main"].output) }
    }
    kotlinSourcesJar {
        // 构件名
        archiveBaseName.set(rootProject.name)
        // 打包子项目源代码
        rootProject.subprojects.forEach { from(it.sourceSets["main"].allSource) }
    }
}

publishing {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://repo.xiao-jie.top/repository/maven-releases")
            credentials {
                username = project.findProperty("repoUser").toString()
                password = project.findProperty("repoPassword").toString()
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = rootProject.name
        }
    }
}