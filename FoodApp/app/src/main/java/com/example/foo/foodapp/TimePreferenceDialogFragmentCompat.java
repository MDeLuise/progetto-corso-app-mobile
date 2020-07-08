package com.example.foo.foodapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;


/**
 * Class representing the dialog for the TimePreference class
 */
public class TimePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    /**
     * The TimePicker widget
     */
    private TimePicker _mTimePicker;
    private SharedPreferences _pref;
    private NotificationScheduler _notificationScheduler;


    /**
     * Creates a new Instance of the TimePreferenceDialogFragment and stores the key of the
     * related Preference
     *
     * @param key The key of the related Preference
     * @return A new Instance of the TimePreferenceDialogFragment
     */
    public static TimePreferenceDialogFragmentCompat newInstance(String key) {
        final TimePreferenceDialogFragmentCompat fragment = new TimePreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        _pref = getActivity().getPreferences(Context.MODE_PRIVATE);

        _mTimePicker = view.findViewById(R.id.edit);

        // Exception: There is no TimePicker with the id 'edit' in the dialog.
        if (_mTimePicker == null) {
            throw new IllegalStateException("Dialog view must contain a TimePicker");
        }

        // Get the time from the related Preference
        Integer minutesAfterMidnight = null;
        DialogPreference preference = getPreference();
        if (preference instanceof TimePreference) {
            minutesAfterMidnight = ((TimePreference) preference).getTime();
        }

        // Set the time to the TimePicker
        if (minutesAfterMidnight != null) {
            int hours = minutesAfterMidnight / 60;
            int minutes = minutesAfterMidnight % 60;
            boolean is24hour = DateFormat.is24HourFormat(getContext());

            _mTimePicker.setIs24HourView(is24hour);
            _mTimePicker.setCurrentHour(hours);
            _mTimePicker.setCurrentMinute(minutes);
        }

        _mTimePicker.setIs24HourView(true);
    }


    /**
     * Called when the Dialog is closed.
     *
     * @param positiveResult Whether the Dialog was accepted or canceled.
     */
    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // Get the current values from the TimePicker
            int hours;
            int minutes;
            if (Build.VERSION.SDK_INT >= 23) {
                hours = _mTimePicker.getHour();
                minutes = _mTimePicker.getMinute();
            } else {
                hours = _mTimePicker.getCurrentHour();
                minutes = _mTimePicker.getCurrentMinute();
            }

            // Generate value to save
            int minutesAfterMidnight = (hours * 60) + minutes;

            // Save the value
            DialogPreference preference = getPreference();
            if (preference instanceof TimePreference) {
                TimePreference timePreference = ((TimePreference) preference);
                // This allows the client to ignore the user value.
                if (timePreference.callChangeListener(minutesAfterMidnight)) {
                    // Save the value
                    timePreference.setTime(minutesAfterMidnight);
                    _pref.edit().putInt("timePicker", minutesAfterMidnight).commit();
                    _notificationScheduler.scheduleNotification();
                }
            }
        }
    }


    public void linkNotificationScheduler(NotificationScheduler scheduler) {
        _notificationScheduler = scheduler;
    }


}