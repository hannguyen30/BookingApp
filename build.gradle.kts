buildscript {
    dependencies {
        classpath(libs.google.services)
        classpath("com.android.tools.build:gradle:8.7.2")
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.google.gms.google-services") version "4.4.1" apply false
}
