package uz.mukhammadakbar.image.viewer;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class PictureData {
    Drawable drawable;
    Bitmap thumbnail;

    PictureData(Drawable drawable, Bitmap thumbnail) {
        this.drawable = drawable;
        this.thumbnail = thumbnail;
    }
}