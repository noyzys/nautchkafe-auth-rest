plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.spring") version "1.9.10"
}

group = "dev.nautchkafe.mojang.rest"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.asynchttpclient:async-http-client:2.12.3")
    implementation("io.vavr:vavr:0.10.4")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}