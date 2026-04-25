import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.frictionwellbeing.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.frictionwellbeing.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    debugImplementation("androidx.compose.ui:ui-tooling")
}

val usageLimitUnitTestClasses = layout.buildDirectory.dir("usageLimitUnitTest/classes")

tasks.register<JavaCompile>("compileUsageLimitUnitTest") {
    source(
        "src/main/java/com/frictionwellbeing/app/FrictionStateCalculator.java",
        "src/main/java/com/frictionwellbeing/app/FrictionChallenge.java",
        "src/main/java/com/frictionwellbeing/app/OverlayFrictionEligibility.java",
        "src/main/java/com/frictionwellbeing/app/OverlayRepeatMode.java",
        "src/main/java/com/frictionwellbeing/app/UsageLimitCalculator.java",
        "src/test/java/com/frictionwellbeing/app/FrictionChallengeTest.java",
        "src/test/java/com/frictionwellbeing/app/FrictionStateCalculatorTest.java",
        "src/test/java/com/frictionwellbeing/app/OverlayFrictionEligibilityTest.java",
        "src/test/java/com/frictionwellbeing/app/OverlayRepeatModeTest.java",
        "src/test/java/com/frictionwellbeing/app/UsageLimitCalculatorTest.java",
    )
    classpath = files()
    destinationDirectory.set(usageLimitUnitTestClasses)
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

tasks.register<JavaExec>("runUsageLimitUnitTest") {
    dependsOn("compileUsageLimitUnitTest")
    classpath = files(usageLimitUnitTestClasses)
    mainClass.set("com.frictionwellbeing.app.UsageLimitCalculatorTest")
}

tasks.register<JavaExec>("runFrictionStateUnitTest") {
    dependsOn("compileUsageLimitUnitTest")
    classpath = files(usageLimitUnitTestClasses)
    mainClass.set("com.frictionwellbeing.app.FrictionStateCalculatorTest")
}

tasks.register<JavaExec>("runFrictionChallengeTest") {
    dependsOn("compileUsageLimitUnitTest")
    classpath = files(usageLimitUnitTestClasses)
    mainClass.set("com.frictionwellbeing.app.FrictionChallengeTest")
}

tasks.register<JavaExec>("runOverlayFrictionEligibilityTest") {
    dependsOn("compileUsageLimitUnitTest")
    classpath = files(usageLimitUnitTestClasses)
    mainClass.set("com.frictionwellbeing.app.OverlayFrictionEligibilityTest")
}

tasks.register<JavaExec>("runOverlayRepeatModeTest") {
    dependsOn("compileUsageLimitUnitTest")
    classpath = files(usageLimitUnitTestClasses)
    mainClass.set("com.frictionwellbeing.app.OverlayRepeatModeTest")
}

tasks.named("check") {
    dependsOn("runUsageLimitUnitTest")
    dependsOn("runFrictionChallengeTest")
    dependsOn("runFrictionStateUnitTest")
    dependsOn("runOverlayFrictionEligibilityTest")
    dependsOn("runOverlayRepeatModeTest")
}
