package com.sovate.workratemanager;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.sovate.workratemanager.bundle.SettingData;
import com.sovate.workratemanager.network.HttpApi;

public class PreferenceSetting extends PreferenceActivity {

    private static final String TAG = "PreferenceSetting";

    private AppCompatDelegate mDelegate;

    SettingData settingData;

    MyPreferenceFragment myPreferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupActionBar();

        myPreferenceFragment = new MyPreferenceFragment();

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content,
                        myPreferenceFragment).commit();

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    // PreferenceFragment 클래스 사용
    public static class MyPreferenceFragment extends
            PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            ListPreference searchEngineSettings = (ListPreference)findPreference("serverUrl");

            //Log.i(TAG, "getValue()" + searchEngineSettings.getValue());

            searchEngineSettings.setSummary(searchEngineSettings.getValue());

            searchEngineSettings.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    preference.setSummary((String) newValue);



                    // return false; 로 리턴하면 변경을 취소합니다.
                    return true;
                }
            });
        }
    }
}