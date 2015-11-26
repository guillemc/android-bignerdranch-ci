package com.bignerdranch.android.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.bignerdranch.android.criminalintent.Crime;
import com.bignerdranch.android.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.util.Date;
import java.util.UUID;

public class CrimeCursorWrapper extends CursorWrapper {

    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime() {
        String uuidString = getString(getColumnIndex(CrimeTable.Cols.UUID));
        String title = getString(getColumnIndex(CrimeTable.Cols.TITLE));
        long date = getLong(getColumnIndex(CrimeTable.Cols.DATE));
        int isSolved = getInt(getColumnIndex(CrimeTable.Cols.SOLVED));
        String suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT));
        String suspectKey = getString(getColumnIndex(CrimeTable.Cols.SUSPECT_KEY));
        long suspectId = getLong(getColumnIndex(CrimeTable.Cols.SUSPECT_ID));

        Crime c = new Crime(UUID.fromString(uuidString));
        c.setTitle(title);
        c.setDate(new Date(date));
        c.setSolved(isSolved != 0);
        c.setSuspect(suspect);
        c.setSuspectKey(suspectKey);
        c.setSuspectId(suspectId);
        return c;
    }
}
