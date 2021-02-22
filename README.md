# ImageViewer
[![](https://jitpack.io/v/markizdeviler/ImageViewer.svg)](https://jitpack.io/#markizdeviler/ImageViewer)

Simple image viewer library. The library uses Glide for image caching

![ezgif-5-3d31992f4f](https://user-images.githubusercontent.com/22816503/46241323-97ab4d00-c3d0-11e8-900c-c970ce420b5b.gif)

## Usage	

### Step 1 
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.markizdeviler:ImageViewer:0.0.1'
}
``` 

### Step 2
``` xml
<uz.mukhammadakbar.image.viewer.ImageViewer
    android:id="@+id/imageViewer"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"/>
```
```java
 imageViewer.imageId(R.drawable.default_image) //default_image for this lib (optional/required)
 imageViewer.imageUrl("image url")             // get image by url (optinal/required)
 imageViewer.errorImg(R.drawable.error_image)  // error image while loading image (optional/required)
 ```

#### Any Issues & contributions appreciated


License
--------

    Copyright 2018 Mukhammadakbar Rafiqov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
