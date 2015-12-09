package com.bignerdranch.android.criminalintent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.io.File;
import java.util.UUID;

public class ImageViewFragment extends DialogFragment {

    private ImageView mImageView;
    private Crime mCrime;

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String TAG = "ImageViewFragment";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        UUID id = (UUID) getArguments().getSerializable(ARG_CRIME_ID);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_image, null);
        mImageView = (ImageView) v.findViewById(R.id.dialog_image);

        mImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int w = mImageView.getWidth();
                int h = mImageView.getHeight();
                Log.d(TAG, "ImageView dimensions: " + w + "x" + h); // for some reason it doesn't get a correct height (it's always zero)
                if (w > 0 || h > 0) {
                    updatePhotoView();
                    mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        mCrime = CrimeLab.get(getActivity()).getCrime(id);

        return new AlertDialog.Builder(getActivity())
                .setView(mImageView)
                .setTitle(mCrime.getTitle())
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

    public static ImageViewFragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, id);

        ImageViewFragment fragment = new ImageViewFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public void updatePhotoView() {
        File imgFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        if (imgFile.exists()) {
            mImageView.setImageBitmap(PictureUtils.getScaledBitmap(imgFile.getPath(), mImageView));
        }
    }
}
