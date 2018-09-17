# ImageViewer

 simple image viewer library

## Usage	

### Step 1 
```android
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}

dependencies {
   implementation 'com.github.markizdeviler:ImageViewer:master'
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
 imageViewer.errorImg(R.drawable.error_image)  // error image while loading image (optional)
 ```

#### Any Issues & contributions appreciated
