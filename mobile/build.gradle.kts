
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.valise.mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.valise.mobile"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "0.10"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField(
            "String",
            "WEB_CLIENT_ID",
            "\"${project.findProperty("WEB_CLIENT_ID")}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }


}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
//    implementation("androidx.activity:activity-ktx:1.2.0")
//    implementation("androidx.fragment:fragment-ktx:1.3.0")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.places)
    implementation(libs.play.services.maps)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.datetime)
    implementation(libs.json)
    implementation("com.github.skydoves:cloudy:0.2.3")

    // Testing
    // Coroutines
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    // Mockito
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")

    testImplementation("junit:junit:4.13.2")

    // Other
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)



    val nav_version = "2.8.4"
    // Jetpack Compose integration
    implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation("androidx.navigation:navigation-ui:$nav_version")
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")

    implementation("androidx.credentials:credentials:1.5.0-alpha06")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0-alpha06")

    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // When using the BoM, you don't specify versions in Firebase library dependencies

    // Add the dependency for the Firebase SDK for Google Analytics
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.firebaseui:firebase-ui-auth:7.2.0")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.firebaseui:firebase-ui-storage:7.2.0")
    implementation( "com.github.bumptech.glide:glide:4.14.2" )
    annotationProcessor( "com.github.bumptech.glide:compiler:4.14.2" )
    implementation ( "com.github.bumptech.glide:compose:1.0.0-beta01" )

    // Google Places dependency
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:2.0.20"))
    implementation("com.google.android.libraries.places:places:3.5.0")
    // images
    implementation("io.coil-kt:coil-compose:2.0.0")

    // Google Maps Compose library
    val mapsComposeVersion = "4.4.1"
    implementation("com.google.maps.android:maps-compose:$mapsComposeVersion")
    // Google Maps Compose utility library
    implementation("com.google.maps.android:maps-compose-utils:$mapsComposeVersion")
    // Google Maps Compose widgets library
    implementation("com.google.maps.android:maps-compose-widgets:$mapsComposeVersion")

    // Junit
    testImplementation(kotlin("test"))

}

// Junit
tasks.withType<Test> {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}


// For Google Places API
secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}

