// WakeUp/build.gradle

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
//    alias(libs.plugins.firebase) apply false // ✅ Use version catalog plugin alias
}
