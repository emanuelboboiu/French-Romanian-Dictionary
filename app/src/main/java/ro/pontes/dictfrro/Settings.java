package ro.pontes.dictfrro;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * Class started on Sunday, 31 May 2015, created by Emanuel Boboiu.
 * This class contains useful methods like save or get settings.
 * */

public class Settings {

    // The file name for save and load preferences:
    private final static String PREFS_NAME = "dfrSettings";

    private final Context context;

    // The constructor:
    public Settings(Context context) {
        this.context = context;
    } // end constructor.

    // A method to detect if a preference exist or not:
    public boolean preferenceExists(String key) {
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.contains(key);
    } // end detect if a preference exists or not.

    // Methods for save and read preferences with SharedPreferences:
    // Save a boolean value:
    public void saveBooleanSettings(String key, boolean value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        // Commit the edits!
        editor.apply();
    } // end save boolean.

    // Read boolean preference:
    public boolean getBooleanSettings(String key) {
        boolean value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.getBoolean(key, false);

        return value;
    } // end get boolean preference from SharedPreference.

    // Save a integer value:
    public void saveIntSettings(String key, int value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        // Commit the edits!
        editor.apply();
    } // end save integer.

    // Read integer preference:
    public int getIntSettings(String key) {
        int value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.getInt(key, 0);

        return value;
    } // end get integer preference from SharedPreference.

    // For float values in shared preferences:
    public void saveFloatSettings(String key, float value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        // Commit the edits!
        editor.apply();
    } // end save float.

    // Read float preference:
    public float getFloatSettings(String key) {
        float value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.getFloat(key, 3.0F); // a default value like the value
        // for moderate magnitude.

        return value;
    } // end get float preference from SharedPreference.

    // Read double preference:
    public double getDoubleSettings(String key) {
        float tempValue;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        tempValue = settings.getFloat(key, 3.0F);

        return tempValue;
    } // end return a double from a float preference.

    // Save a String value:
    public void saveStringSettings(String key, String value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        // Commit the edits!
        editor.apply();
    } // end save String.

    // Read String preference:
    public String getStringSettings(String key) {
        String value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.getString(key, null);

        return value;
    } // end get String preference from SharedPreference.
    // End read and write settings in SharedPreferences.

    // Charge Settings function:
    public void chargeSettings() {

        // Determine if is first launch of the program:
        boolean isNotFirstRunning = getBooleanSettings("isFirstRunning");

        if (!isNotFirstRunning) {
            saveBooleanSettings("isFirstRunning", true);
            // Make default values in SharedPrefferences:
            setDefaultSettings();
        }

        // Now charge settings:

        // Play or not the sounds and speech:
        MainActivity.isSpeech = getBooleanSettings("isSpeech");
        MainActivity.isSound = getBooleanSettings("isSound");
        MainActivity.isSearchFullText = getBooleanSettings("isSearchFullText");

        // For search history:
        if (preferenceExists("isHistory")) {
            MainActivity.isHistory = getBooleanSettings("isHistory");
        } // end if isHistory preference exists.

        // For done button of the keyboard to send a try:
        MainActivity.isImeAction = getBooleanSettings("isImeAction");

        // For text size:
        MainActivity.textSize = getIntSettings("textSize");

        // For the background of the activities:
        MainActivity.background = getStringSettings("background");

        // Is shake detector or not:
        MainActivity.isShake = getBooleanSettings("isShake");
        // The magnitude of the shake detector:
        // MainActivity.onshakeMagnitude =
        // getFloatSettings("onshakeMagnitude");;

        // Wake lock, keep screen awake:
        MainActivity.isWakeLock = getBooleanSettings("isWakeLock");

        // For search direction:
        MainActivity.direction = getIntSettings("direction");

        // Charge the premium status:
        MainActivity.isPremium = getBooleanSettings("isPremium");

        /* About number of launches, useful for information, rate and others: */
        // Get current number of launches:
        MainActivity.numberOfLaunches = getIntSettings("numberOfLaunches");
        // Increase it by one:
        MainActivity.numberOfLaunches++;
        // Save the new number of launches:
        saveIntSettings("numberOfLaunches", MainActivity.numberOfLaunches);
    } // end charge settings.

    public void setDefaultSettings() {

        // // Activate speech, sounds for dice and number speaking:
        saveBooleanSettings("isSpeech", true);
        saveBooleanSettings("isSound", true);
        saveBooleanSettings("isSearchFullText", false);
        saveBooleanSettings("isImeAction", true);

        // For text size for lines:
        saveIntSettings("textSize", 20);

        saveBooleanSettings("isHistory", true);

        // Activate shake detection:
        saveBooleanSettings("isShake", true);

        // Set on shake magnitude to 3.0F: // now default value, medium.
        saveFloatSettings("onshakeMagnitude", 3.0F);

        // For keeping screen awake:
        saveBooleanSettings("isWakeLock", false);

        // Save DataBases version to 0:
        saveIntSettings("dbVer", 0);

        // For search direction:
        saveIntSettings("direction", 0);

        // We make it again as being not a premium one, to remove after:
        saveBooleanSettings("isPremium", false);

        // To be considered not noticed about premium version:
        saveBooleanSettings("wasNoticedPremium", false);

        /*
         * Save current engine to empty string, this way we will have again
         * Google TTS:
         */
        saveStringSettings("curEngine", "");

        // Save language, country and variant:
        saveStringSettings("ttsLanguage", "");
        saveStringSettings("ttsCountry", "");
        saveStringSettings("ttsVariant", "");
        saveFloatSettings("ttsRate", 1.0F);
        saveFloatSettings("ttsPitch", 1.0F);

        // The background must be saved to null:
        MainActivity.background = null;
        saveStringSettings("background", MainActivity.background);

    } // end setDefaultSettings function.

} // end Settings Class.
