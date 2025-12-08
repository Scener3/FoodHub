plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    kotlin("jvm")
}

group = "org.FoodHub"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application{
    mainClass.set("org.FoodHub.Launcher")
}

javafx {
    version = "25"
    modules = listOf( "javafx.controls", "javafx.fxml" )
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.20.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation(kotlin("stdlib-jdk8"))
}


tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(24)
}