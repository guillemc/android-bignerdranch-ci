package com.bignerdranch.android.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class CrimeListActivity extends SingleFragmentActivity
    implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks {

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        if (findViewById(R.id.detail_fragment_container) == null) { // one pane
            Intent intent = CrimePagerActivity.newIntent(this, crime.getId());
            startActivity(intent);
        } else { // two panes
            updateDetailFragment(crime);
        }
    }

    @Override
    public void onCrimeStatusChanged(Crime crime) {
        CrimeFragment crimeFragment = (CrimeFragment) getSupportFragmentManager().findFragmentById(R.id.detail_fragment_container);
        if (crimeFragment != null && crimeFragment.getCrimeId().equals(crime.getId())) {
            crimeFragment.updateCheckBox(crime.isSolved());
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        updateListFragment();
    }

    @Override
    public void onCrimeDeleted(Crime crime) {
        updateListFragment();
        removeDetailFragment();
    }

    private void updateListFragment() {
        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }

    private void updateDetailFragment(Crime crime) {
        Fragment newDetail = CrimeFragment.newInstance(crime.getId());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_fragment_container, newDetail)
                .commit();
    }

    private void removeDetailFragment() {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .remove(fm.findFragmentById(R.id.detail_fragment_container))
                .commit();
    }


}
