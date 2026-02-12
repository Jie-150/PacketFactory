import io.izzel.taboolib.gradle.BukkitNMSUtil
import io.izzel.taboolib.gradle.TabooLibExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    `java-library`
    kotlin("jvm") version "2.0.21" apply false
    id("io.izzel.taboolib") version "2.0.27" apply false
}
subprojects {
    apply<JavaPlugin>()
    apply(plugin = "io.izzel.taboolib")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    configure<TabooLibExtension> {
        subproject = true
        env {
            install(BukkitNMSUtil)
        }
        version { taboolib = "6.2.4-e6c8347" }
    }
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        compileOnly(kotlin("stdlib"))
        // server
        compileOnly("com.google.code.gson:gson:2.8.7")
        compileOnly("ink.ptms.core:v11605:11605")
        compileOnly("ink.ptms:nms-all:1.0.0")
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
            freeCompilerArgs = listOf("-Xjvm-default=all", "-Xextended-compiler-checks")
        }
    }
    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

gradle.buildFinished {
    layout.buildDirectory.get().asFile.deleteRecursively()
}