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
    jar {
//        destinationDirectory.set(file("H:/1.18.1/plugins"))
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
        // API 发布配置
        create<MavenPublication>("api") {
            groupId = "org.craft.packetfactory"
            artifactId = "api"
            // 使用 taboolibBuildApi 任务的输出
            artifact("${project.buildDir}/libs/${rootProject.name}-${rootProject.version}-api.jar")
            // 添加 sources jar
            artifact(tasks.
            named("kotlinSourcesJar")) {
                classifier = "sources"
            }
        }
    }
}