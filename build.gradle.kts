plugins {
    id("java")
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    implementation("io.github.spair:imgui-java-app:1.92.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("logicSimulator.Main")
}