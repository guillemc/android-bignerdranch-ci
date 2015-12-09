package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.util.Log;
import android.view.View;

import java.io.IOException;

public class PictureUtils {

    public static String TAG = "PictureUtils";

    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }

    public static Bitmap getScaledBitmap(String path, View v) {
        return getScaledBitmap(path, v.getWidth(), v.getHeight());
    }

    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        // read in the dimensions of the image on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;

        int orientation = getImageOrientation(path);
        int rotationAngle;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90 :
                rotationAngle = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180 :
                rotationAngle = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270 :
                rotationAngle = 270;
                break;
            default:
                rotationAngle = 0;
        }
        if (rotationAngle == 90 || rotationAngle == 270) {
            srcWidth = options.outHeight;
            srcHeight = options.outWidth;
        }

        // figure out how much to scale down by
        int inSampleSize = 1;

        if (srcHeight > destHeight || srcWidth > destWidth) {
            final int halfHeight = srcHeight / 2;
            final int halfWidth = srcWidth / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > destHeight && (halfWidth / inSampleSize) > destWidth) {
                inSampleSize *= 2;
            }

        }
        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        Log.d(TAG, String.format("Scaling %s (%dx%d) for %dx%d (sample size: %d)",
                path, srcWidth, srcHeight, destWidth, destHeight, inSampleSize));

        Bitmap bm = BitmapFactory.decodeFile(path, options);
        return rotateBitmap(bm, rotationAngle);
    }

    public static int getImageOrientation(String path) {
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try {
            ExifInterface ei = new ExifInterface(path);
            String orientationString = ei.getAttribute(ExifInterface.TAG_ORIENTATION);
            if (orientationString != null) {
                orientation = Integer.parseInt(orientationString);
            }
        } catch(IOException e) {
            Log.e(TAG, "Unable to get orientation: " + path, e);
        }
        return orientation;
    }

    public static Bitmap rotateBitmap(Bitmap bm, int rotationAngle) {
        if (rotationAngle == 0) {
            return bm;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
    }
}
