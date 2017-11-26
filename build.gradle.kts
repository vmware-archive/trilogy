
buildscript {
    val kotlinVersion = "1.1.60"
    extra["kotlinVersion"] = kotlinVersion

    val springBootVersion = "1.5.6.RELEASE"
    extra["springBootVersion"] = springBootVersion

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
    }
}

apply {
    plugin("kotlin")
    plugin("org.springframework.boot")
}

plugins {
    application
}

application {
    mainClassName = "io.pivotal.trilogy.application.TrilogyApplication"
}

repositories {
    jcenter()
    maven { setUrl("http://repository.jetbrains.com/all/") }
}


val oracleBootstrapClasses = "io/pivotal/trilogy/live/oracle/bootstrap/**"
val oracleBootstrap = task<Test>("oracleBootstrap") {
    description = "Create common testing objects in an Oracle database"
    include(oracleBootstrapClasses)
}

val oracleTests = task<Test>("oracleTests") {
    description = "End to end tests against an Oracle database"
    dependsOn(oracleBootstrap)
    include("io/pivotal/trilogy/live/oracle/**")
    exclude(oracleBootstrapClasses)
}

val postgresTests = task<Test>("postgresTests") {
    description = "End to end tests against a Postgres database"
    include("io/pivotal/trilogy/live/postgres/**")
}

val test = tasks.getByName("test") as Test
test.apply {
    exclude("io/pivotal/trilogy/live/**")
}

task<Test>("testAll") {
    description = "Run all unit and Oracle end to end tests"
    dependsOn(oracleTests, test)
}


configure<JavaPluginConvention> {
    setSourceCompatibility(1.7)
    setTargetCompatibility(1.7)
}

val kotlinVersion = extra["kotlinVersion"] as String
val springBootVersion = extra["springBootVersion"] as String

dependencies {
    compile(kotlin("stdlib", version = kotlinVersion))
    compile(kotlin("reflect", version = kotlinVersion))
    compile("org.springframework.boot:spring-boot-starter-jdbc")
    compile("org.springframework.boot:spring-boot-starter:$springBootVersion")
    compile("commons-cli:commons-cli:1.3.1")
    compile("org.flywaydb:flyway-core:4.0.3")

    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile("org.jetbrains.spek:spek:1.0.+")
    testCompile("org.amshove.kluent:kluent:1.4")
}
