import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

val dbHost = System.getenv("DB_MASTER_HOST") ?: "localhost"
val dbPort = System.getenv("DB_MASTER_PORT") ?: "15432"
val dbUser = System.getenv("DB_USER") ?: "root"
val dbPass = System.getenv("DB_PASS") ?: "root"
val dbName = System.getenv("DB_NAME") ?: "db"

liquibase {
    activities.register("main") {
        this.arguments = mapOf(
                "logLevel" to "info",
                "changelogFile" to "/src/main/resources/liquibase/changelog.xml",
                "url" to "jdbc:postgresql://$dbHost:$dbPort/$dbName",
                "username" to dbUser,
                "password" to dbPass
        )
    }
}

jooq {
    version.set("3.18.2")
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)

    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
                    user = dbUser
                    password = dbPass
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "com.example.attendance.driver.attendancedb"
                        directory = "build/generated-src/jooq/main"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

plugins {
    id("org.springframework.boot") version "3.1.4"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"
    id("org.openapi.generator") version "6.2.0"
    id("org.liquibase.gradle") version  "2.2.0"
    id("nu.studer.jooq") version "8.2"
    kotlin("plugin.serialization") version "1.9.10"
    id("io.kotest.multiplatform") version "5.0.2"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    /**
     * liquibaseRuntime
     */
    implementation("org.liquibase:liquibase-core")
    liquibaseRuntime("info.picocli:picocli:4.7.1")
    runtimeOnly("org.postgresql:postgresql")
    liquibaseRuntime("org.liquibase:liquibase-core")
    liquibaseRuntime("org.postgresql:postgresql")
    /**
     * Swagger Annotations
     * Swagger Models
     * Jakarta Annotations API
     */
    compileOnly("io.swagger.core.v3:swagger-annotations:2.2.4")
    compileOnly("io.swagger.core.v3:swagger-models:2.2.4")
    compileOnly("jakarta.annotation:jakarta.annotation-api:2.1.1")
    /**
     * Spring Boot Starter Validation
     */
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("javax.servlet:javax.servlet-api:3.1.0")
    implementation("javax.validation:validation-api:2.0.1.Final")
    jooqGenerator("org.postgresql:postgresql:42.5.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    /**
     * kotest
     */
    val kotestVersion = "5.7.2"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    /**
     * Arrow kt
     */
    implementation("io.arrow-kt:arrow-core:1.2.0")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.2.0")
    /**
     * okhttp
     */
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    /**
     * csv
     */
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

/**
 * OpenAPI Generator を使って API ドキュメント生成
 */
task<GenerateTask>("generateApiDoc") {
    generatorName.set("html2")
    inputSpec.set("$projectDir/openapi.yaml")
    outputDir.set("$buildDir/openapi/doc/")
}

/**
 * OpenAPI Generator を使ってコード生成
 */
task<GenerateTask>("generateApiServer") {
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/openapi.yaml")
    outputDir.set("$buildDir/openapi/server-code/") // .gitignoreされているので注意(わざとここにあります)
    apiPackage.set("com.example.attendance_api.openapi.generated.controller") // 各自のアプリケーションに合わせてパス名を変更する
    modelPackage.set("com.example.attendance_api.openapi.generated.model") // 各自のアプリケーションに合わせてパス名を変更する
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
        )
    )
    /**
     * true にすると tags 準拠で、API の interface を生成する
     */
    additionalProperties.set(
        mapOf(
            "useTags" to "true"
        )
    )
}

/**
 * Kotlinをコンパイルする前に、generateApiServerタスクを実行
 * 必ずスキーマファイルから最新のコードが生成され、もし変更があったらコンパイル時に失敗して気付けるため
 */
tasks.compileKotlin {
    dependsOn("generateApiServer")
}

/**
 * OpenAPI Generator によって生成されたコードを import できるようにする
 */
kotlin.sourceSets.main {
    kotlin.srcDir("$buildDir/openapi/server-code/src/main")
}