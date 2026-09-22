# jOOQ OpenAPI & Lombok Generator

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://www.java.com/)
[![jOOQ](https://img.shields.io/badge/jOOQ-3.21.x-blue.svg)](https://www.jooq.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)

Кастомный генератор кода для **jOOQ**, который автоматически обогащает сгенерированные POJO-классы аннотациями **OpenAPI (Swagger)** и **Lombok**.

## 🚀 Возможности

При генерации кода библиотека автоматически:
1. Добавляет аннотацию `@Schema(description = "...")` к классу
2. Добавляет аннотацию `@Schema(description = "...", required = true/false)` к каждому полю
3. Добавляет аннотации Lombok: `@Builder(toBuilder = true)` и `@NoArgsConstructor`

## 📦 Подключение

### 1. Настройка репозитория (GitHub Packages)

Добавьте в `build.gradle.kts`:

    repositories {
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/isToxic/jooq-openapi-generator")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }

### 2. Добавление зависимости

    dependencies {
        jooqGenerator("io.github.isToxic:jooq-openapi-generator:1.0.0")
        implementation("io.swagger.core.v3:swagger-annotations:2.2.25")
        compileOnly("org.projectlombok:lombok:1.18.48")
        annotationProcessor("org.projectlombok:lombok:1.18.48")
    }

## ⚙️ Настройка jOOQ

В конфигурации плагина jOOQ укажите кастомный генератор:

    jooq {
        configuration {
            generator {
                strategy {
                    name = "is.toxic.SwaggerJavaGenerator"
                }
            }
        }
    }

## 💡 Пример результата

**До (стандартный jOOQ):**

    public class UserRecord {
        private final Long id;
        private final String username;
    }

**После (с генератором):**

    @Schema(description = "Таблица пользователей системы")
    @lombok.Builder(toBuilder = true)
    @lombok.NoArgsConstructor
    public class UserRecord {
        @Schema(description = "Уникальный идентификатор", required = true)
        private final Long id;
        
        @Schema(description = "Логин пользователя", required = true)
        private final String username;
    }

## 🛠 Требования

- **Java**: 25+
- **jOOQ**: 3.21.x
- **База данных**: PostgreSQL

## 📄 Лицензия

Apache License 2.0. См. файл [LICENSE](LICENSE).

## 👤 Автор

**Stepan Mokrov**  
GitHub: [@isToxic](https://github.com/isToxic)
