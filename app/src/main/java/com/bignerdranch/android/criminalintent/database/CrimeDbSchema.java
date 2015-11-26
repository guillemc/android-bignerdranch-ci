package com.bignerdranch.android.criminalintent.database;


public class CrimeDbSchema {
    public static final class CrimeTable {

        public static final String NAME = "Crimes";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
            public static final String SUSPECT = "suspect";
            public static final String SUSPECT_KEY = "suspect_key";
            public static final String SUSPECT_ID = "suspect_id";
        }
    }
}
