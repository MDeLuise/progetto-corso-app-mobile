package com.example.foo.foodapp;

import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.foo.foodapp.database.AppDatabase;
import com.example.foo.foodapp.database.FoodDAO;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Class implementing the preferences fragment
 */
public class SettingsFragment extends PreferenceFragmentCompat implements AppFragment {

    private SharedPreferences _settingsPreference;
    private FoodDAO _db;
    private NotificationScheduler _notificationScheduler;


    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.app_preferences);

        _settingsPreference = getDefaultSharedPreferences(getContext());
        linkDB();

        setupResetButtons();
        setupSwitchNotifications();
        setupSwitchOldFood();

        hideNotificationSectionIfOldAndroid();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    /**
     * Link the notification manager for the app
     * @param scheduler the notification manager to use
     */
    public void linkNotificationScheduler(NotificationScheduler scheduler) {
        _notificationScheduler = scheduler;
    }


    /**
     * Get the notification used for this class
     * @return notification used
     */
    public SharedPreferences getPreference() {
        return _settingsPreference;
    }


    /**
     * Link the database to this class
     */
    private void linkDB() {
        _db = Room.databaseBuilder(getContext(), AppDatabase.class, getString(R.string.database_name))
                .allowMainThreadQueries()
                .build().getFoodDAO();
    }


    /**
     * Setup the three last button in the view
     */
    private void setupResetButtons() {
        Preference removeCacheButton = findPreference("cacheButton");
        removeCacheButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.attention_string))
                        .setMessage(R.string.remove_cache)
                        .setPositiveButton(getString(R.string.affermative),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        _db.removeHiddenFoods();
                                        Toast.makeText(getContext(),
                                                getString(R.string.cache_removed), Toast.LENGTH_SHORT).show();
                                    }
                                })
                        .setNegativeButton(getString(R.string.negative), null)
                        .show();
                return true;
            }
        });

        Preference defaultPreferencesButton = findPreference("defaultSettingsButton");
        defaultPreferencesButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.attention_string))
                        .setMessage(R.string.default_settings)
                        .setPositiveButton(getString(R.string.affermative),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        setDefaultPreferences();
                                        Toast.makeText(getContext(),
                                                getString(R.string.default_settings_done), Toast.LENGTH_SHORT).show();
                                    }
                                })
                        .setNegativeButton(getString(R.string.negative), null)
                        .show();
                return true;
            }
        });

        Preference removeDataButton = findPreference("defaultButton");
        removeDataButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.attention_string))
                        .setMessage(R.string.erase_all)
                        .setPositiveButton(getString(R.string.affermative),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        eraseAllData();
                                        Toast.makeText(getContext(),
                                                getString(R.string.erase_all_done), Toast.LENGTH_SHORT).show();
                                    }
                                })
                        .setNegativeButton(getString(R.string.negative), null)
                        .create();

                alertDialog.show();

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(getResources().getColor(R.color.dialog_cancel_bgcolor));
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(getResources().getColor(R.color.dialog_ok_bgcolor));

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.dialog_text_color));
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.dialog_text_color));
                return true;
            }

        });
    }


    /**
     * Setup the notifications switch present in this view
     */
    private void setupSwitchNotifications() {
        Preference notificationSwitch = findPreference("notificationSwitch");

        notificationSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Preference timePicker = findPreference("timePicker");
                timePicker.setEnabled(_settingsPreference.getBoolean("notificationSwitch", true));

                Preference daysPicker = findPreference("notificationDays");
                daysPicker.setEnabled(_settingsPreference.getBoolean("notificationSwitch", true));
                return true;
            }
        });
        hideSettingsIfRelativeSwitchOff();
    }


    /**
     * Setup the old food switch present in this view
     */
    private void setupSwitchOldFood() {
        Preference oldFoodSwitch = findPreference("removeOldFoodSwitch");

        oldFoodSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Preference editText = findPreference("RemoveOldDays");
                editText.setEnabled(_settingsPreference.getBoolean("removeOldFoodSwitch", true));

                Preference checkBox = findPreference("notificationAboutAutoRemoveFood");
                checkBox.setEnabled(_settingsPreference.getBoolean("removeOldFoodSwitch", true));
                return true;
            }
        });

        hideSettingsIfRelativeSwitchOff();
    }


    /**
     * Hide the setting related to a switch which is turned off
     */
    private void hideSettingsIfRelativeSwitchOff() {
        findPreference("timePicker").setEnabled(
                _settingsPreference.getBoolean("notificationSwitch", false));
        findPreference("notificationDays").setEnabled(
                _settingsPreference.getBoolean("notificationSwitch", false));

        findPreference("RemoveOldDays").setEnabled(
                _settingsPreference.getBoolean("removeOldFoodSwitch", false));
        findPreference("notificationAboutAutoRemoveFood").setEnabled(
                _settingsPreference.getBoolean("removeOldFoodSwitch", false));
    }


    /**
     * Show the custom time picker
     * @param preference preference where to put the inserted data
     */
    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Try if the preference is one of our custom Preferences

        if (preference instanceof TimePreference) {
            // Create a new instance of TimePreferenceDialogFragment with the key of the related
            // Preference
            TimePreferenceDialogFragmentCompat timePickerFragment =
                    TimePreferenceDialogFragmentCompat.newInstance(preference.getKey());
            timePickerFragment.linkNotificationScheduler(_notificationScheduler);
            timePickerFragment.setTargetFragment(this, 0);
            timePickerFragment.show(this.getFragmentManager(),
                    "android.support.v7.preference" +
                            ".PreferenceFragment.DIALOG");
        }

        // Could not be handled here. Try with the super method.
        else {
            super.onDisplayPreferenceDialog(preference);
        }
    }


    /**
     * Implements what to do when the user click on "reset preferences" button
     */
    private void setDefaultPreferences() {
        _settingsPreference.edit().clear().apply();

        // have to manually refresh the preferences because if not, they'll be refreshed only
        // after activity is re-created
        ((CheckBoxPreference) findPreference("oldFoodInserted")).setChecked(
                _settingsPreference.getBoolean("oldFoodInserted",false));
        ((ListPreference) findPreference("currency")).setValue(
                _settingsPreference.getString("currency", "€"));
        ((SwitchPreferenceCompat) findPreference("removeOldFoodSwitch")).setChecked(
                _settingsPreference.getBoolean("removeOldFoodSwitch",false));
        ((EditTextPreference) findPreference("RemoveOldDays")).setText(
                _settingsPreference.getString("RemoveOldDays","1"));
        ((CheckBoxPreference) findPreference("notificationAboutAutoRemoveFood")).setChecked(
                _settingsPreference.getBoolean("notificationAboutAutoRemoveFood",false));
        ((SwitchPreferenceCompat) findPreference("notificationSwitch")).setChecked(
                _settingsPreference.getBoolean("notificationSwitch",false));
        ((EditTextPreference) findPreference("notificationDays")).setText(
                _settingsPreference.getString("notificationDays","1"));
        ((TimePreference) findPreference("timePicker")).setTime(
                _settingsPreference.getInt("timePicker",540));
        ((TimePreference) findPreference("timePicker")).onSetInitialValue(
                true, _settingsPreference.getInt("timePicker",540));

        hideSettingsIfRelativeSwitchOff();

        _notificationScheduler.scheduleNotification();
    }


    /**
     * Implements what to do when the user click on "erase all data" button
     */
    private void eraseAllData() {
        setDefaultPreferences();
        _db.initializeFoodsTable();
        _db.initializeFavoritesTable();
    }


    /**
     * Hide the settings notifications category if installed android is less then Oreo
     */
    private void hideNotificationSectionIfOldAndroid() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            findPreference("notificationCategory").setVisible(false);
            findPreference("notificationSwitch").setVisible(false);
            findPreference("notificationDays").setVisible(false);
            findPreference("timePicker").setVisible(false);
        }
    }


    @Override
    public void manageFloatingButton(FloatingActionButton fab) {}


    @Override
    public RecyclerView getRView() {
        return getListView();
    }


    @Override
    public DeactivableViewPager getMainActivityViewPager() {
        return ((MainActivity) getActivity())._viewPager;
    }


    @Override
    public void refresh() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }


}
