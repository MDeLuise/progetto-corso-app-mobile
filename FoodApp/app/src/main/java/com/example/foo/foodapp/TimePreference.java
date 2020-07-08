package com.example.foo.foodapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;


/**
 * Class representing the custom Time Picker (handle by TimePreferenceDialogFragmentCompact class)
 */
public class TimePreference extends DialogPreference {

    private int _mTime;


    public TimePreference(Context context) {
        this(context, null);
    }


    public TimePreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceStyle);
    }


    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }


    private TimePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    /**
     * Gets the time from the Shared Preferences
     *
     * @return The current preference value
     */
    public int getTime() {
        return _mTime;
    }


    /**
     * Saves the time to the SharedPreferences
     *
     * @param time The time to save
     */
    public void setTime(int time) {
        _mTime = time;

        // Save to SharedPreference
        persistInt(time);
    }


    /**
     * Called when a Preference is being inflated and the default value attribute needs to be read
     */
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // The type of this preference is Int, so we read the default value from the attributes
        // as Int. Fallback value is set to 0.
        return a.getInt(index, 0);
    }


    /**
     * Returns the layout resource that is used as the content View for the dialog
     */
    @Override
    public int getDialogLayoutResource() {
        return R.layout.pref_dialog_time;
    }


    /**
     * Implement this to set the initial value of the Preference.
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        // If the value can be restored, do it. If not, use the default value.
        setTime(restorePersistedValue ?
                getPersistedInt(_mTime) : (int) defaultValue);
    }

}