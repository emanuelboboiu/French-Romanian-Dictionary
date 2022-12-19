package ro.pontes.dictfrro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class SettingsActivity extends Activity {

    final Context mFinalContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Some initial things like background:
        GUITools.setLayoutInitial(this, 2);

        // Check or check the check boxes, depending of current boolean values:

        // For sounds in program:
        CheckBox cbtSoundsSetting = findViewById(R.id.cbtSoundsSetting);
        cbtSoundsSetting.setChecked(MainActivity.isSound);

        // For search full text in program:
        CheckBox cbtSearchFullTextSetting = findViewById(R.id.cbtSearchFullTextSetting);
        cbtSearchFullTextSetting.setChecked(MainActivity.isSearchFullText);

        // For shake:
        CheckBox cbtOnshakeSetting = findViewById(R.id.cbtOnshakeSetting);
        cbtOnshakeSetting.setChecked(MainActivity.isShake);

        // For keeping screen awake:
        CheckBox cbtScreenAwakeSetting = findViewById(R.id.cbtScreenAwakeSetting);
        cbtScreenAwakeSetting.setChecked(MainActivity.isSpeech);

        // For IME DONE button of the keyboard:
        CheckBox cbtImeSetting = findViewById(R.id.cbtImeSetting);
        cbtImeSetting.setChecked(MainActivity.isImeAction);

        // For search history, activated or not:
        CheckBox cbtHistorySetting = findViewById(R.id.cbtHistorySetting);
        cbtHistorySetting.setChecked(MainActivity.isHistory);
    } // end onCreate() method.

    // Let's see what happens when a check box is clicked in audio settings:
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        Settings set = new Settings(this); // to save changes.

        // Check which check box was clicked
        switch (view.getId()) {
            case R.id.cbtSoundsSetting:
                MainActivity.isSound = checked;
                set.saveBooleanSettings("isSound", MainActivity.isSound);
                break;

            case R.id.cbtSearchFullTextSetting:
                MainActivity.isSearchFullText = checked;
                set.saveBooleanSettings("isSearchFullText",
                        MainActivity.isSearchFullText);
                break;

            case R.id.cbtOnshakeSetting:
                MainActivity.isShake = checked;
                set.saveBooleanSettings("isShake", MainActivity.isShake);
                break;
            case R.id.cbtScreenAwakeSetting:
                MainActivity.isWakeLock = checked;
                set.saveBooleanSettings("isWakeLock", MainActivity.isWakeLock);
                break;
            case R.id.cbtImeSetting:
                MainActivity.isImeAction = checked;
                set.saveBooleanSettings("isImeAction", MainActivity.isImeAction);
                break;

            case R.id.cbtHistorySetting:
                if (checked) {
                    MainActivity.isHistory = true;
                } else {
                    MainActivity.isHistory = false;
                    // Try here to delete also the log:
                    deleteLog();
                }
                set.saveBooleanSettings("isHistory", MainActivity.isHistory);
                break;
        } // end switch.

        // Play also a sound:
        SoundPlayer.playSimple(this, "element_clicked");
    } // end onClick method.

    public void deleteLog() {
        String tempTitle = getString(R.string.sh_title_delete_history);
        String tempBody = getString(R.string.sh_disable_now);
        new AlertDialog.Builder(this)
                .setTitle(tempTitle)
                .setMessage(tempBody)
                .setIcon(android.R.drawable.ic_delete)
                .setPositiveButton(R.string.yes,
                        (dialog, whichButton) -> {
                            SearchHistory searchHistory = new SearchHistory(
                                    mFinalContext);
                            searchHistory.deleteSearchHistory();
                            // Play a delete sound:
                            SoundPlayer.playSimple(mFinalContext,
                                    "delete_history");
                        }).setNegativeButton(R.string.no, null).show();

    } // end deleteLog() method.

    @Override
    public void onBackPressed() {
        this.finish();
        GUITools.goToDictionary(this);
    } // end onBackPressed()

} // end settings activity class.
