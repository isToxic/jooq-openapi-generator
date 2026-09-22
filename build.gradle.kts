plugins {
    `java-library`
    `maven-publish`
}

group = "com.github.isToxic"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.jooq)
    api(libs.jooq.codegen)
    api(libs.jooq.meta)
    api(libs.swagger.annotations)
    api(libs.postgresql)
    api(libs.jakarta.annotation.api)
}

java {
    sourceCompatibility = JavaVersion.VERSION_25

    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("jOOQ OpenAPI & Lombok Generator")
                description.set("Custom jOOQ code generator that injects Swagger/OpenAPI and Lombok annotations into POJOs")
                url.set("https://github.com/isToxic/jooq-openapi-generator")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("isToxic")
                        name.set("Stepan Mokrov")
                        email.set("s.b.mokrov@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/isToxic/jooq-openapi-generator.git")
                    developerConnection.set("scm:git:ssh://github.com:isToxic/jooq-openapi-generator.git")
                    url.set("https://github.com/isToxic/jooq-openapi-generator")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/istoxic/jooq-openapi-generator")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GH_USERNAME")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("GH_TOKEN")
            }
        }
    }
}
