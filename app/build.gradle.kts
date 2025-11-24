import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    google()
    mavenCentral()
}

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "org.kepocnhh.x509"
    compileSdk = 35

    defaultConfig {
        applicationId = namespace
        minSdk = 24
        targetSdk = compileSdk
        versionCode = 1
        versionName = "0.0.$versionCode"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".$name"
            versionNameSuffix = "-$name"
            isMinifyEnabled = false
            isShrinkResources = false
            manifestPlaceholders["buildType"] = name
        }
    }

    buildFeatures.buildConfig = true

    productFlavors {
        mapOf(
            "specifics" to setOf("mock"),
        ).forEach { (dimension, names) ->
            flavorDimensions += dimension
            names.forEach { name ->
                create(name) {
                    this.dimension = dimension
                    applicationIdSuffix = ".$name"
                    versionNameSuffix = "-$name"
                }
            }
        }
        create("real") {
            this.dimension = "specifics"
        }
    }

    packaging {
        resources.excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF") // bouncycastle
    }
}

androidComponents.onVariants { variant ->
    val output = variant.outputs.single()
    check(output is com.android.build.api.variant.impl.VariantOutputImpl)
    output.outputFileName = listOf(
        rootProject.name,
        android.defaultConfig.versionName!!,
        variant.name,
        android.defaultConfig.versionCode!!.toString(),
    ).joinToString(separator = "-", postfix = ".apk")
    afterEvaluate {
        tasks.getByName<JavaCompile>("compile${variant.name.replaceFirstChar(Character::toUpperCase)}JavaWithJavac") {
            targetCompatibility = "17"
        }
        tasks.getByName<KotlinCompile>("compile${variant.name.replaceFirstChar(Character::toUpperCase)}Kotlin") {
            kotlinOptions.jvmTarget = "17"
        }
    }
}

dependencies {
    implementation("androidx.activity:activity:1.10.1")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.82")
}
