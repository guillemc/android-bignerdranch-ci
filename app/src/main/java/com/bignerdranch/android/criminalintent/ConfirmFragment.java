package com.bignerdranch.android.criminalintent;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class ConfirmFragment extends DialogFragment{

    private int mTitleId;
    private DialogInterface.OnClickListener mListener;

    public static ConfirmFragment newInstance(int titleId, DialogInterface.OnClickListener listener) {
        ConfirmFragment fragment = new ConfirmFragment();
        fragment.mTitleId = titleId;
        fragment.mListener = listener;
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(mTitleId)
                .setPositiveButton(android.R.string.ok, mListener)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
