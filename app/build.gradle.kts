/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
}

android {
  compileSdk = libs.versions.compileSdk.get().toInt()
  buildFeatures {
    dataBinding = true
  }
  defaultConfig {
    applicationId = "com.google.samples.apps.sunflower"
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()
    testInstrumentationRunner = "com.google.samples.apps.sunflower.utilities.MainTestRunner"
    versionCode = 1
    versionName = "0.1.6"
    vectorDrawables.useSupportLibrary = true

    // Consult the README on instructions for setting up Unsplash API key
    buildConfigField("String", "UNSPLASH_ACCESS_KEY", "\"" + getUnsplashAccess() + "\"")
    javaCompileOptions {
      annotationProcessorOptions {
        arguments["dagger.hilt.disableModulesHaveInstallInCheck"] = "true"
      }
    }
  }
  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
    }
    create("benchmark") {
      initWith(getByName("release"))
      signingConfig = signingConfigs.getByName("debug")
      isDebuggable = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules-benchmark.pro"
      )
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    // work-runtime-ktx 2.1.0 and above now requires Java 8
    jvmTarget = JavaVersion.VERSION_17.toString()

    // Enable Coroutines and Flow APIs
    freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlinx.coroutines.FlowPreview"
  }
  buildFeatures {
    compose = true
    dataBinding = true
    buildConfig = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
  }
  packagingOptions {
    // Multiple dependency bring these files in. Exclude them to enable
    // our test APK to build (has no effect on our AARs)
    resources.excludes += "/META-INF/AL2.0"
    resources.excludes += "/META-INF/LGPL2.1"
  }

  testOptions {
    managedDevices {
      devices {
        maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2api27").apply {
          device = "Pixel 2"
          apiLevel = 27
          systemImageSource = "aosp"
        }
      }
    }
  }
  namespace = "com.google.samples.apps.sunflower"
}

androidComponents {
  onVariants(selector().withBuildType("release")) {
    // Only exclude *.version files in release mode as debug mode requires
    // these files for layout inspector to work.
    it.packaging.resources.excludes.add("META-INF/*.version")
  }
}

dependencies {
  ksp(libs.androidx.room.compiler)
  ksp(libs.hilt.android.compiler)
  implementation(libs.bundles.retrofit)
  implementation(libs.bundles.coroutines)
  implementation(libs.bundles.hilt)
  implementation(libs.bundles.androidx.impl)
  implementation(libs.bundles.lifecycle)
  implementation(libs.bundles.room)
  implementation(libs.material)
  implementation(libs.gson)
  implementation(libs.okhttp3.logging.interceptor)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.androidx.paging.compose)

  // Compose
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.bundles.compose)
  implementation(libs.glide)
  debugImplementation(libs.androidx.compose.ui.tooling)

  // Testing dependencies
  androidTestImplementation(libs.bundles.espresso)
  androidTestImplementation(libs.bundles.androidx.test)
  debugImplementation(libs.androidx.monitor)
  kspAndroidTest(libs.hilt.android.compiler)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.guava)
  androidTestImplementation(libs.hilt.android.testing)
  androidTestImplementation(libs.accessibility.test.framework)
  androidTestImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.junit)
}

fun getUnsplashAccess(): String? {
  return project.findProperty("unsplash_access_key") as? String
}
