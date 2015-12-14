package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private Crime mCrime;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mDeleteButton;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;

    private static final String TAG = "CrimeFragment";
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_REMOVE = "DialogRemove";
    private static final String DIALOG_IMAGE = "DialogImage";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;

    private static final int LOADER_QUERY_CONTACT = 100;
    private static final int LOADER_QUERY_PHONE = 101;

    private Callbacks mCallbacks;

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
        void onCrimeDeleted(Crime crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        CrimeLab lab = CrimeLab.get(getActivity());
        mCrime = lab.getCrime(crimeId);
        mPhotoFile = lab.getPhotoFile(mCrime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        updateCheckBox(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //
            }
        });

        mDeleteButton = (Button) v.findViewById(R.id.crime_delete);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCrime();
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* this is how it's normally done:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
                */
                // this is using the share intent builder from the support library:
                ShareCompat.IntentBuilder.from(getActivity())
                    .setChooserTitle(R.string.send_report)
                    .setSubject(getString(R.string.crime_report_subject))
                    .setType("text/plain")
                    .setText(getCrimeReport())
                    .startChooser();
            }
        });

        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        PackageManager pm = getActivity().getPackageManager();
        // Check if there is an application in the device that can respond
        // to the pickContact intent. Only activities with the CATEGORY_DEFAULT flag
        // respond to implicit intents
        // NOTE: it's a little easier to check it the other way around: pickContact.resolveActivity(pm)
        if (pm.resolveActivity(pickContact, pm.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        } else {
            mSuspectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(pickContact, REQUEST_CONTACT);
                }
            });
        }

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        mCallButton = (Button) v.findViewById(R.id.call_suspect);
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // option 1: show contact
                    /*
                    Uri contactUri = ContactsContract.Contacts.getLookupUri(mCrime.getSuspectId(), mCrime.getSuspectKey());
                    Intent viewContact = new Intent(Intent.ACTION_VIEW);
                    viewContact.setData(contactUri);
                    startActivity(viewContact);
                    */

                // option 2: query phone number + dial intent
                getLoaderManager().restartLoader(LOADER_QUERY_PHONE, null, CrimeFragment.this);
            }
        });
        mCallButton.setEnabled(mCrime.getSuspectKey() != null);

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);

        mPhotoView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                // Now we could reliably get its dimensions:
                // int w = mPhotoView.getWidth();
                // int h = mPhotoView.getHeight();
                updatePhotoView();

                // Once data has been obtained, this listener is no longer needed, so remove it...
                mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            ImageViewFragment dialog = ImageViewFragment.newInstance(mCrime.getId());
            dialog.show(fm, DIALOG_IMAGE);
            }
        });

        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(pm) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        if (canTakePhoto) {
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete:
                removeCrime();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
            updateCrime();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            Log.d(TAG, "REQUEST_CONTACT activity result: " + contactUri.toString());
            Bundle args = new Bundle();
            args.putString("contactUri", contactUri.toString());
            getLoaderManager().restartLoader(LOADER_QUERY_CONTACT, args, this);
        } else if (requestCode == REQUEST_PHOTO) {
            updatePhotoView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    private void updateDate() {
        java.text.DateFormat df = DateFormat.getLongDateFormat(getContext());
        mDateButton.setText(df.format(mCrime.getDate()));
    }

    private void removeCrime() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ConfirmFragment dialog = ConfirmFragment.newInstance(R.string.crime_delete_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Activity activity = getActivity();
                CrimeLab.get(activity).removeCrime(mCrime);
                if (activity.findViewById(R.id.detail_fragment_container) == null) {
                    activity.finish();
                } else {
                    mCallbacks.onCrimeDeleted(mCrime);
                }
            }
        });
        dialog.show(fm, DIALOG_REMOVE);
    }

    private String getCrimeReport() {
        String solvedString = mCrime.isSolved() ? getString(R.string.crime_report_solved)
                : getString(R.string.crime_report_unsolved);

        String dateString = DateFormat.format("EEE, MMM dd", mCrime.getDate()).toString();
        String suspect = mCrime.getSuspect() == null ? getString(R.string.crime_report_no_suspect)
                : getString(R.string.crime_report_suspect, mCrime.getSuspect());
        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mPhotoView);
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    private void updateCrime() {
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    public void updateCheckBox(Boolean checked) {
        mSolvedCheckBox.setChecked(checked);
    }

    public UUID getCrimeId() {
        return mCrime.getId();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri contentUri;
        String[] queryFields;
        String selectClause;
        String[] selectParams;

        switch (id) {
            case LOADER_QUERY_PHONE:
                contentUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // this query requires android.permission.READ_CONTACTS
                queryFields = new String[]{ ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.NUMBER };
                selectClause = ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ?";
                selectParams = new String[] { mCrime.getSuspectKey() };
                return new CursorLoader(
                        getActivity(),
                        contentUri,
                        queryFields,
                        selectClause,
                        selectParams,
                        null             // Default sort order
                );
            case LOADER_QUERY_CONTACT:
                contentUri = Uri.parse(args.getString("contactUri"));
                queryFields = new String[] { ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.Contacts.LOOKUP_KEY,
                        ContactsContract.Contacts._ID };
                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        contentUri,
                        queryFields,
                        null,
                        null,
                        null
                );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        if (c == null || c.getCount() < 1) {
            Log.d(TAG, "Empty cursor for loader " + loader.getId());
            return;
        }
        switch (loader.getId()) {
            case LOADER_QUERY_CONTACT:
                setContactData(c);
                updateCrime();
                break;
            case LOADER_QUERY_PHONE:
                String phone = getPhoneFromCursor(c);
                if (phone != null) {
                    Intent dial = new Intent(Intent.ACTION_DIAL);
                    dial.setData(Uri.parse("tel:" + phone));
                    startActivity(dial);
                }
                break;
        }
    }

    private void setContactData(Cursor c) {
        try {
            c.moveToFirst();
            String suspect = c.getString(0);
            mCrime.setSuspect(suspect);
            mCrime.setSuspectKey(c.getString(1));
            mCrime.setSuspectId(Long.parseLong(c.getString(2)));
            mSuspectButton.setText(suspect);
            mCallButton.setEnabled(mCrime.getSuspectKey() != null);
            Log.d(TAG, mCrime.getSuspect() + ": " + mCrime.getSuspectKey() + "/" + mCrime.getSuspectId());
        } catch (Exception e) {
            Log.e(TAG, "Unable to update suspect with contact data", e);
        }
    }

    private String getPhoneFromCursor(Cursor c) {
        String phone = null;
        try {
            while (c.moveToNext()) {
                int type = c.getInt(0);
                String number = c.getString(1);
                if (!number.isEmpty()) {
                    if (type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE || phone == null) {
                        phone = number;
                    }
                }
            }
            Log.d(TAG, "Phone: " + phone);
        } catch (Exception e) {
            Log.e(TAG, "Error getting phone", e);
        }
        return phone;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //  In this callback, you should delete all references to the current Cursor in order to prevent memory leaks
    }
}
