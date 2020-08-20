# Fee

![](/app/src/main/res/mipmap-hdpi/ic_launcher.png)

A sample android app that shows how to use [RxBle](https://github.com/Polidea/RxAndroidBle) :rocket:, ViewModels, LiveData with RxJava2 & Hilt, in Kotlin by "Clean Architecture".


### Implemented by
The structure of this project with 3 layers:
- ui
- domain
- data

### Communication between layers

1. UI calls method from ViewModel.
2. ViewModel executes internal logic.
3. Internal logic combines data from Repo.
4. The Repo = BLEClient for this one
5. Information flows back to the UI by LiveData.

### Dependencies

```gradle

    // kotlin
    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"

    // androidx
    implementation 'androidx.appcompat:appcompat:1.2.0'
    implementation 'androidx.core:core-ktx:1.3.1'
    implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.2.0'
    implementation 'androidx.cardview:cardview:1.0.0'
    implementation 'androidx.recyclerview:recyclerview:1.1.0'
    implementation 'androidx.fragment:fragment-ktx:1.2.5'

    // google
    implementation 'com.google.android.material:material:1.2.0'

    // hilt
    implementation 'com.google.dagger:hilt-android:2.28-alpha'
    kapt 'com.google.dagger:hilt-android-compiler:2.28-alpha'
    implementation 'androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-SNAPSHOT'
    kapt 'androidx.hilt:hilt-compiler:1.0.0-SNAPSHOT'

    // ble
    implementation "com.polidea.rxandroidble2:rxandroidble:1.11.1"

    // rx
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.0'

    // test
    testImplementation 'junit:junit:4.13'
    androidTestImplementation 'androidx.test.ext:junit:1.1.1'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.2.0'
```
