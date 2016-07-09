/*
 * Copyright (C) 2016 Glucosio Foundation
 *
 * This file is part of Glucosio.
 *
 * Glucosio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Glucosio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Glucosio.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.glucosio.android.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.MenuItem;
import android.widget.EditText;

import org.glucosio.android.BuildConfig;
import org.glucosio.android.GlucosioApplication;
import org.glucosio.android.R;
import org.glucosio.android.db.DatabaseHandler;
import org.glucosio.android.db.User;
import org.glucosio.android.tools.InputFilterMinMax;
import org.glucosio.android.tools.LocaleHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);



        getFragmentManager().beginTransaction()
                .replace(R.id.preferencesFrame, new MyPreferenceFragment()).commit();

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(getString(R.string.action_settings));
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        private DatabaseHandler dB;
        private User user;
        private ListPreference languagePref;
        private ListPreference countryPref;
        private ListPreference genderPref;
        private ListPreference diabetesTypePref;
        private ListPreference unitPrefGlucose;
        private ListPreference unitPrefA1c;
        private ListPreference unitPrefWeight;
        private ListPreference rangePref;
        private EditText ageEditText;
        private EditText minEditText;
        private EditText maxEditText;
        private EditTextPreference agePref;
        private EditTextPreference minRangePref;
        private EditTextPreference maxRangePref;
        private SwitchPreference dyslexiaModePref;
        private User updatedUser;
        private LocaleHelper localeHelper;


        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            final GlucosioApplication app = (GlucosioApplication) getActivity().getApplicationContext();
            dB = app.getDBHandler();
            localeHelper = app.getLocaleHelper();
            user = dB.getUser(1);
            updatedUser = new User(user.getId(), user.getName(), user.getPreferred_language(),
                    user.getCountry(), user.getAge(), user.getGender(), user.getD_type(),
                    user.getPreferred_unit(), user.getPreferred_unit_a1c(),
                    user.getPreferred_unit_weight(), user.getPreferred_range(),
                    user.getCustom_range_min(), user.getCustom_range_max());
            agePref = (EditTextPreference) findPreference("pref_age");
            countryPref = (ListPreference) findPreference("pref_country");
            languagePref = (ListPreference) findPreference("pref_language");
            genderPref = (ListPreference) findPreference("pref_gender");
            diabetesTypePref = (ListPreference) findPreference("pref_diabetes_type");
            unitPrefGlucose = (ListPreference) findPreference("pref_unit_glucose");
            unitPrefA1c = (ListPreference) findPreference("pref_unit_a1c");
            unitPrefWeight = (ListPreference) findPreference("pref_unit_weight");
            rangePref = (ListPreference) findPreference("pref_range");
            minRangePref = (EditTextPreference) findPreference("pref_range_min");
            maxRangePref = (EditTextPreference) findPreference("pref_range_max");
            dyslexiaModePref = (SwitchPreference) findPreference("pref_font_dyslexia");

            agePref.setDefaultValue(user.getAge());
            countryPref.setValue(user.getCountry());
            genderPref.setValue(user.getGender());
            diabetesTypePref.setValue(user.getD_type() + "");
            unitPrefGlucose.setValue(user.getPreferred_unit());
            unitPrefA1c.setValue(user.getPreferred_unit_a1c());
            unitPrefWeight.setValue(user.getPreferred_unit_weight());
            rangePref.setValue(user.getPreferred_range());

            minRangePref.setDefaultValue(user.getCustom_range_min() + "");
            maxRangePref.setDefaultValue(user.getCustom_range_max() + "");
            minRangePref.setDefaultValue(user.getCustom_range_min() + "");
            maxRangePref.setDefaultValue(user.getCustom_range_max() + "");

            if (!"custom".equals(rangePref.getValue())) {
                minRangePref.setEnabled(false);
                maxRangePref.setEnabled(false);
            } else {
                minRangePref.setEnabled(true);
                maxRangePref.setEnabled(true);
            }

            final Preference aboutPref = findPreference("about_settings");
            countryPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updatedUser.setCountry(newValue.toString());

                    updateDB();
                    return false;
                }
            });
            agePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().trim().equals("")) {
                        return false;
                    }
                    updatedUser.setAge(Integer.parseInt(newValue.toString()));
                    updateDB();
                    return true;
                }
            });
            genderPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updatedUser.setGender(newValue.toString());
                    updateDB();
                    return true;
                }
            });
            diabetesTypePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().equals(getResources().getString(R.string.helloactivity_spinner_diabetes_type_1))) {
                        updatedUser.setD_type(1);
                        updateDB();
                    } else {
                        updatedUser.setD_type(2);
                        updateDB();
                    }
                    return true;
                }
            });
            unitPrefGlucose.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updatedUser.setPreferred_unit(newValue.toString());
                    updateDB();
                    return true;
                }
            });
            unitPrefA1c.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().equals(getResources().getString(R.string.preferences_spinner_preferred_a1c_unit_1))) {
                        updatedUser.setPreferred_unit_a1c("percentage");
                    } else {
                        updatedUser.setPreferred_unit_a1c("mmol/mol");
                    }
                    updateDB();
                    return true;
                }
            });
            unitPrefWeight.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().equals(getResources().getString(R.string.preferences_spinner_preferred_weight_unit_1))) {
                        updatedUser.setPreferred_unit_weight("kilograms");
                    } else {
                        updatedUser.setPreferred_unit_weight("pounds");
                    }
                    updateDB();
                    return true;
                }
            });
            rangePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updatedUser.setPreferred_range(newValue.toString());
                    updateDB();
                    return true;
                }
            });
            minRangePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().trim().equals("")) {
                        return false;
                    }
                    updatedUser.setCustom_range_min(Integer.parseInt(newValue.toString()));
                    updateDB();
                    return true;
                }
            });
            maxRangePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().trim().equals("")) {
                        return false;
                    }
                    updatedUser.setCustom_range_max(Integer.parseInt(newValue.toString()));
                    updateDB();
                    return true;
                }
            });
            dyslexiaModePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // EXPERIMENTAL PREFERENCE
                    // Display Alert
                    showExperimentalDialog(true);
                    return true;
                }
            });

            ageEditText = agePref.getEditText();
            minEditText = minRangePref.getEditText();
            maxEditText = maxRangePref.getEditText();

            ageEditText.setFilters(new InputFilter[]{new InputFilterMinMax(1, 110)});
            minEditText.setFilters(new InputFilter[]{new InputFilterMinMax(1, 1500)});
            maxEditText.setFilters(new InputFilter[]{new InputFilterMinMax(1, 1500)});

            // Get countries list from locale
            ArrayList<String> countriesArray = new ArrayList<>();
            Locale[] locales = Locale.getAvailableLocales();

            for (Locale locale : locales) {
                String country = locale.getDisplayCountry();
                if (country.trim().length() > 0 && !countriesArray.contains(country)) {
                    countriesArray.add(country);
                }
            }
            Collections.sort(countriesArray);

            CharSequence[] countries = countriesArray.toArray(new CharSequence[countriesArray.size()]);
            countryPref.setEntryValues(countries);
            countryPref.setEntries(countries);

            initLanguagePreference();

            updateDB();

            aboutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent aboutActivity = new Intent(getActivity(), AboutActivity.class);
                    getActivity().startActivity(aboutActivity);
                    return false;
                }
            });
        }

        private void initLanguagePreference() {
            String[] languages = BuildConfig.TRANSLATION_ARRAY;
            Set<String> availableLanguagesSet = new HashSet<>();
            // We always support english
            availableLanguagesSet.add("en");

            // Get english string to confront
            // I know, it's a weird workaround
            // Sorry :/
            String englishString = "Automatic backup";

            for (String localString : languages) {
                // For each locale, check if we have translations
                Resources res = getResources();
                Configuration conf = res.getConfiguration();
                Locale savedLocale = conf.locale;
                conf.locale = localeHelper.getLocale(localString);
                res.updateConfiguration(conf, null);

                // Retrieve an example string from this locale
                String localizedString = res.getString(R.string.activity_backup_drive_automatic);

                if (!englishString.equals(localizedString)){
                    // if english string is not the same of localized one
                    // a translation is available
                    availableLanguagesSet.add(localString);
                }

                // restore original locale
                conf.locale = savedLocale;
                res.updateConfiguration(conf, null);
            }

            List<String> availableLanguagesList = new ArrayList<>(availableLanguagesSet);
            Collections.sort(availableLanguagesList);

            List<String> valuesLanguages = new ArrayList<>(availableLanguagesList.size());
            List<String> displayLanguages = new ArrayList<>(availableLanguagesList.size());
            for (String language : availableLanguagesList) {
                if (language.length() > 0) {
                    valuesLanguages.add(language);
                    displayLanguages.add(localeHelper.getDisplayLanguage(language));
                }
            }

            languagePref.setEntryValues(getEntryValues(valuesLanguages));
            languagePref.setEntries(getEntryValues(displayLanguages));

            String languageValue = user.getPreferred_language();
            if (languageValue != null) {
                languagePref.setValue(languageValue);
                String displayLanguage = localeHelper.getDisplayLanguage(languageValue);
                languagePref.setSummary(displayLanguage);
            }

            languagePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    String language = (String) newValue;
                    updatedUser.setPreferred_language(language);
                    languagePref.setSummary(localeHelper.getDisplayLanguage(language));
                    languagePref.setValue(language);

                    updateDB();

                    localeHelper.updateLanguage(getActivity(), language);
                    getActivity().recreate();

                    return true;
                }
            });
        }

        private void updateDB() {
            dB.updateUser(updatedUser);
            agePref.setSummary(user.getAge() + "");
            genderPref.setSummary(user.getGender() + "");
            diabetesTypePref.setSummary(getResources().getString(R.string.glucose_reading_type) + " " + user.getD_type());
            unitPrefGlucose.setSummary(user.getPreferred_unit() + "");
            unitPrefA1c.setSummary(user.getPreferred_unit_a1c() + "");
            unitPrefWeight.setSummary(user.getPreferred_unit_weight() + "");
            countryPref.setSummary(user.getCountry());
            minRangePref.setSummary(user.getCustom_range_min() + "");
            maxRangePref.setSummary(user.getCustom_range_max() + "");

            countryPref.setValue(user.getCountry());
            genderPref.setValue(user.getGender());
            diabetesTypePref.setValue(user.getD_type() + "");
            unitPrefGlucose.setValue(user.getPreferred_unit());
            genderPref.setValue(user.getGender());
            unitPrefGlucose.setValue(user.getPreferred_unit());
            rangePref.setValue(user.getPreferred_range());

            if (!user.getPreferred_range().equals("Custom range")) {
                minRangePref.setEnabled(false);
                maxRangePref.setEnabled(false);
            } else {
                minRangePref.setEnabled(true);
                maxRangePref.setEnabled(true);
            }
        }

        private void showExperimentalDialog(final boolean restartRequired) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.preferences_experimental_title))
                    .setMessage(R.string.preferences_experimental)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (restartRequired) {
                                rebootApp();
                            }
                        }
                    })
                    .show();
        }

        private void rebootApp() {
            Intent mStartActivity = new Intent(getActivity().getApplicationContext(), MainActivity.class);
            int mPendingIntentId = 123456;
            PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity().getApplicationContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) getActivity().getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
            System.exit(0);
        }
    }

    @NonNull
    private static String[] getEntryValues(List<String> list) {
        String[] result = new String[list.size()];
        return list.toArray(result);
    }
}