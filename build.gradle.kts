@file:Suppress("HasPlatformType")

plugins {
    java
}

val shim by sourceSets.creating
val packaged by configurations.creating

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.4")

    add(shim.implementationConfigurationName, "com.sun.xml.bind:jaxb-core:4.0.0")

    packaged("javax.xml.bind:jaxb-api:2.4.0-b180830.0359") { isTransitive = false }
    packaged("com.sun.xml.bind:jaxb-core:4.0.0") { isTransitive = false }
}

val shimJar by tasks.creating(Jar::class) {
    archiveBaseName.set("jaxb-core-shim")
    from(shim.output)
}

tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME) {
    manifest {
        attributes(mapOf("Premain-Class" to "dev.necauqua.scm.Agent"))
    }

    // add the packaged jars and our shim
    from(listOf(packaged, shimJar)) {
        into("extraLibs")
    }

    // embed actual agent dependencies (well, only asm) as a fat jar
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    // licensing
    from("LICENSE", "LICENSE-asm")
}
