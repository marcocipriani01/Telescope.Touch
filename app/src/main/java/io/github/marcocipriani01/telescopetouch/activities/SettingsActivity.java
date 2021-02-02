/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01) and the Sky Map Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.marcocipriani01.telescopetouch.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;
import io.github.marcocipriani01.telescopetouch.activities.util.DarkerModeManager;

/**
 * Edit the user's preferences.
 */
public class SettingsActivity extends AppCompatActivity implements Preference.OnPreferenceChangeListener {

    @Inject
    SharedPreferences preferences;
    private Preference latitudePref;
    private Preference longitudePref;
    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent == null) return;
                    String latitude = intent.getStringExtra(ApplicationConstants.LATITUDE_PREF), longitude = intent.getStringExtra(ApplicationConstants.LONGITUDE_PREF);
                    if ((latitude == null) || (longitude == null)) return;
                    preferences.edit().putString(ApplicationConstants.LATITUDE_PREF, latitude).putString(ApplicationConstants.LONGITUDE_PREF, longitude).apply();
                    if (latitudePref != null)
                        latitudePref.setSummary(latitude);
                    if (longitudePref != null)
                        longitudePref.setSummary(longitude);
                }
            });
    private AppPreferenceFragment preferenceFragment;
    private DarkerModeManager darkerModeManager;
    private Preference gyroPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((TelescopeTouchApp) getApplication()).getApplicationComponent().inject(this);
        darkerModeManager = new DarkerModeManager(getWindow(), null, PreferenceManager.getDefaultSharedPreferences(this));
        preferenceFragment = new AppPreferenceFragment();
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, preferenceFragment).commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        gyroPref = Objects.requireNonNull(preferenceFragment.findPreference(ApplicationConstants.DISABLE_GYRO_PREF));
        gyroPref.setOnPreferenceChangeListener(this);
        latitudePref = Objects.requireNonNull(preferenceFragment.findPreference(ApplicationConstants.LATITUDE_PREF));
        latitudePref.setOnPreferenceChangeListener(this);
        longitudePref = Objects.requireNonNull(preferenceFragment.findPreference(ApplicationConstants.LONGITUDE_PREF));
        longitudePref.setOnPreferenceChangeListener(this);
        enableGyroPrefs(preferences.getBoolean(ApplicationConstants.DISABLE_GYRO_PREF, false));
        latitudePref.setSummary(preferences.getString(ApplicationConstants.LATITUDE_PREF, "0.0"));
        longitudePref.setSummary(preferences.getString(ApplicationConstants.LONGITUDE_PREF, "0.0"));
        Objects.<Preference>requireNonNull(preferenceFragment.findPreference("pick_location_map"))
                .setOnPreferenceClickListener(preference -> {
                    resultLauncher.launch(new Intent(SettingsActivity.this, MapsActivity.class));
                    return true;
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        darkerModeManager.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        darkerModeManager.stop();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void enableGyroPrefs(boolean enabled) {
        Objects.<Preference>requireNonNull(preferenceFragment.findPreference(ApplicationConstants.REVERSE_MAGNETIC_Z_PREF)).setEnabled(enabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == latitudePref) {
            String string = (String) newValue;
            try {
                double latitude = Double.parseDouble(string);
                if ((latitude > 90.0) || (latitude < -90.0)) {
                    Toast.makeText(this, R.string.out_of_bounds_loc_error, Toast.LENGTH_SHORT).show();
                } else {
                    latitudePref.setSummary(string);
                    return true;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.malformed_loc_error, Toast.LENGTH_SHORT).show();
            }
        } else if (preference == longitudePref) {
            String string = (String) newValue;
            try {
                double longitude = Double.parseDouble(string);
                if ((longitude > 180.0) || (longitude < -180.0)) {
                    Toast.makeText(this, R.string.out_of_bounds_loc_error, Toast.LENGTH_SHORT).show();
                } else {
                    longitudePref.setSummary(string);
                    return true;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.malformed_loc_error, Toast.LENGTH_SHORT).show();
            }
        } else if (preference == gyroPref) {
            enableGyroPrefs(((Boolean) newValue));
            return true;
        }
        return false;
    }

    public static class AppPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preference_screen);
        }
    }
}