rootProject.name = "kotlin-spring-data-rest-movies"

pluginManagement {
	repositories {
		maven { url = uri("https://repo.spring.io/milestone") }
		maven { url = uri("https://repo.spring.io/snapshot") }
		gradlePluginPortal()
	}

	val kotlinVersion: String by settings
	val springBootVersion: String by settings
	val springBootDependencyManagementVersion: String by settings
	val graalVMBuildToolsVersion: String by settings

	plugins {
		id("org.springframework.boot") version springBootVersion
		id("io.spring.dependency-management") version springBootDependencyManagementVersion
		id("org.graalvm.buildtools.native") version graalVMBuildToolsVersion
		id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
		kotlin("jvm") version kotlinVersion
		kotlin("plugin.spring") version kotlinVersion
		kotlin("plugin.jpa") version kotlinVersion
		kotlin("kapt") version kotlinVersion
	}
}
