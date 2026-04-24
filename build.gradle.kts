// Top-level build file where you can add configuration options common to all sub-projects/modules.
// This sets up global plugins (like Android application, Kotlin Compose, and Google Services) 
// without immediately applying them, allowing module-level build files to opt-in or customize them.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}