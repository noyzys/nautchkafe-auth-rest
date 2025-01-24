plugins {
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
}

group = "dev.nautchkafe.mojang.rest"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.asynchttpclient:async-http-client:3.0.1")
    implementation("io.vavr:vavr:0.10.5")
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}