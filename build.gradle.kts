import io.izzel.taboolib.gradle.Bukkit
import io.izzel.taboolib.gradle.BukkitNMSDataSerializer
import io.izzel.taboolib.gradle.BukkitNMSItemTag
import io.izzel.taboolib.gradle.BukkitNMSUtil
import io.izzel.taboolib.gradle.BukkitUtil
import io.izzel.taboolib.gradle.TabooLibExtension
import io.izzel.taboolib.gradle.XSeries
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    `java-library`
    kotlin("jvm") version "2.0.21" apply false
    id("io.izzel.taboolib") version "2.0.30" apply false
}
subprojects {
    apply<JavaPlugin>()
    apply(plugin = "io.izzel.taboolib")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    configure<TabooLibExtension> {
        subproject = true
        env {
            install(BukkitNMSUtil,BukkitNMSItemTag, BukkitNMSDataSerializer, Bukkit)
        }
        version { taboolib = "6.3.0-716e043" }
    }
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        compileOnly(kotlin("stdlib"))
        // server
        compileOnly("com.google.code.gson:gson:2.8.7")
        compileOnly("com.mojang:brigadier:1.0.500")
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