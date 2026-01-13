package ro.pontes.dictfrro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/*
 * Class started on Friday, 04 September 2015, created by Emanuel Boboiu.
 * This is the main class of this application.
 * */

public class MainActivity extends Activity {

    // The following fields are used for the shake detection:
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    // End fields declaration for shake detector.

    public final static String EXTRA_MESSAGE = "ro.pontes.dictfrro.MESSAGE";

    private DBAdapter mDbHelper;
    public static boolean isSpeech = true;
    public static boolean isSound = true;
    public static boolean isShake = true;
    public static float onshakeMagnitude = 2.5F;
    public static boolean isWakeLock = false;
    public static boolean isImeAction = true;
    public static boolean isSearchFullText = false;
    public static boolean isHistory = true;
    public static int textSize = 20; // for TextViews.
    public static String background = null;
    public static int resultsLimit = 300;
    public static int mPaddingDP = 3; // for padding at text views of results.
    public static int direction = 0; // 0 en_ro, 1 ro_en.
    private static final String myUniqueId = "xyzxyzxyz890890890";
    public static boolean isPremium = true;
    private final String mProduct = "frd.premium";
    public static String mUpgradePrice = "�";
    private int idSection = 0;
    public static int numberOfLaunches = 0;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    // public String spoken;

    final Context mFinalContext = this;

    private SpeakText speak;

    private SearchHistory searchHistory;

    private StringTools st;

    private String[] aDirection; // for text above edit.
    private String[] aSpeechDirection; // for recognise prompt. edit.

    private int searchedWords = 0; // increment to post the number.

    // Controls used globally in the application:
    LinearLayout llResults; // for central part of the activity.
    private LinearLayout llBottomInfo = null;

    /*
     * We need a global variable TextView for of a result. A value will be
     * attributed in onCreateContextMenu, and will be read in
     * onContextItemSelected:
     */
    private TextView tvResultForContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Charge settings:
        Settings set = new Settings(this);
        set.chargeSettings();

        // We charge different layouts depending of the premium status:
        if (isPremium) {
            setContentView(R.layout.activity_main_premium);
        } else {
            setContentView(R.layout.activity_main);
        } // end charging the correct layout.

        // Calculate the pixels in DP for mPaddingDP, for TextViews of the
        // results:
        int paddingPixel = 3;
        float density = getResources().getDisplayMetrics().density;
        mPaddingDP = (int) (paddingPixel * density);
        // end calculate mPaddingDP

        // To keep screen awake:
        if (MainActivity.isWakeLock) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } // end wake lock.

        // Start things for our database:
        mDbHelper = new

                DBAdapter(this);
        mDbHelper.createDatabase();
        mDbHelper.open();

        Resources res = getResources();
        aDirection = res.getStringArray(R.array.direction_array);
        aSpeechDirection = res.getStringArray(R.array.speech_direction_array);

        // Find the llResults:
        llResults =

                findViewById(R.id.llResults);

        // Charge the bottom linear layout:
        llBottomInfo = findViewById(R.id.llBottomInfo);

        speak = new

                SpeakText(this);

        searchHistory = new

                SearchHistory(this);

        // Other things at onCreate:
        // a method found in this class.
        updateGUIFirst();

        // ShakeDetector initialisation:
        mSensorManager = (SensorManager)

                getSystemService(Context.SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new

                ShakeDetector();
        mShakeDetector.setShakeThresholdGravity(MainActivity.onshakeMagnitude);
        /*
         * Method you would use to setup whatever you want done once the
         * device has been shook.
         */
        mShakeDetector.setOnShakeListener(this::handleShakeEvent);
        // End initialisation of the shake detector.

        GUITools.checkIfRated(this);
        GUITools.checkIfNoticedAboutPremium(this);

        // Some lines for detecting if search is from history region:
        String historyMessage = getIntent().getStringExtra("wordFromHistory");
        if (historyMessage != null && !historyMessage.isEmpty()) {
            int historyDirection = Integer.parseInt(historyMessage.substring(historyMessage.length() - 1));
            String historyWord = historyMessage.substring(0, historyMessage.length() - 1);
            searchFromHistory(historyWord, historyDirection);
        }
        // end search from history via intent.

        if (!isPremium) {
            // For billing:
            // startBillingDependencies();
            // Initializing the AdView object
            // bannerAdView = findViewById(R.id.bannerAdView);
            // adMobSequence();
        }
    } // end onCreate() method.

    @Override
    public void onResume() {
        super.onResume();

        // Some initial things like background:
        GUITools.setLayoutInitial(this, 1);

        /*
         * We need here StringTools object because some time the keyboard can be
         * changed without recreating the activity:
         */
        st = new StringTools(this);

        if (MainActivity.isShake) {
            /*
             * Add the following line to register the Session Manager Listener
             * onResume:
             */
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    } // end onResume method.

    @Override
    public void onPause() {
        // Add here what you want to happens on pause:
        postStatistics();
        if (MainActivity.isShake) {
            // Add the following line to unregister the Sensor Manager onPause:
            mSensorManager.unregisterListener(mShakeDetector);
        }
        super.onPause();
    } // end onPause method.

    @Override
    protected void onDestroy() {
        // Close the database connection:
        mDbHelper.close();

        // Shut down also the TTS:
        speak.close();
        super.onDestroy();
    } // end onDestroy method.

    @Override
    public void onBackPressed() {
        this.finish();
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    } // end onBackPressed()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    } // end onCreateOptionsMenu() method.

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.mnuActionSettings) {
            goToSettings();
        } else if (id == R.id.mnuTTSSettings) {
            goToTTSSettings();
        } else if (id == R.id.mnuDisplaySettings) {
            goToDisplaySettings();
        } else if (id == R.id.mnuSearchHistory) {
            GUITools.goToHistory(this);
        } else if (id == R.id.mnuBackgroundSettings) {
            goToBackgroundSettings();
        } else if (id == R.id.mnuVocabulary) {
            GUITools.goToVocabulary(this);
        } else if (id == R.id.mnuGoToWebPage) {
            GUITools.goToAppWebPage(this);
        } else if (id == R.id.mnuAppInPlayStore) {
            GUITools.openAppInPlayStore(this);

        } else if (id == R.id.mnuGetPremiumVersion) {
            upgradeAlert();
        } // end if upgrade to premium version was pressed.
        else if (id == R.id.mnuResetDefaults) {
            resetToDefaults();
        } // end if is for set to defaults clicked in main menu.
        else if (id == R.id.mnuHelp) {
            GUITools.showHelp(this);
        } // end if Help is chosen in main menu.
        else if (id == R.id.mnuAboutDialog) {
            GUITools.aboutDialog(this);
        } // end if about game is chosen in main menu.
        else if (id == R.id.mnuRate) {
            GUITools.showRateDialog(this);
        } // end if rate option was chosen in menu.

        return super.onOptionsItemSelected(item);
    } // end onOptionsItemSelected() method.

    // The implementations for context menu:
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(getString(R.string.cm_result_title));
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.results_context_menu, menu);

        // We store globally the text view clicked longly:
        tvResultForContext = (TextView) v;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // First we take the text from the longly clicked result:
        String result = tvResultForContext.getText().toString();
        String[] aResult = result.split(" – ");
        String w = aResult[0];
        String e = aResult[1];

        @SuppressWarnings("unused")
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        int id = item.getItemId();

        if (id == R.id.cmSpeakResult) {
            speakResult(w, e);
            return true;
        } else if (id == R.id.cmSpellResult) {
            spellResult(w, e);
            return true;
        } else if (id == R.id.cmCopyResult) {
            GUITools.copyIntoClipboard(this, result);
            return true;
        } else if (id == R.id.cmCopyWord) {
            GUITools.copyIntoClipboard(this, w);
            return true;
        } else if (id == R.id.cmCopyExplanation) {
            GUITools.copyIntoClipboard(this, e);
            return true;
        } else if (id == R.id.cmAddToVocabularyResult) {
            addToVocabulary(w, e);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    // End context menu implementation.

    // Some methods to go to other activities from menu:
    private void goToSettings() {
        // Called when the user clicks the settings option in menu:
        Intent intent = new Intent(this, SettingsActivity.class);
        String message;
        message = "Francaise Dictionary"; // without a reason, just to be
        // something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end go to settings method.

    private void goToTTSSettings() {
        // Called when the user clicks the Voice settings option in menu:
        Intent intent = new Intent(this, TTSSettingsActivity.class);
        String message;
        message = "Francaise Dictionary"; // without a reason.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end go to display settings method.

    private void goToDisplaySettings() {
        // Called when the user clicks the display settings option in menu:
        Intent intent = new Intent(this, DisplaySettingsActivity.class);
        String message;
        message = "Francaise Dictionary"; // without a reason, just to be
        // something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end go to display settings method.

    private void goToBackgroundSettings() {
        // Called when the user clicks the background settings option in menu:
        Intent intent = new Intent(this, BackgroundActivity.class);
        String message;
        message = "Francaise Dictionary"; // without a reason.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end go to background settings method.

    // End methods to go in other activities from menu.

    // A method which is called when shaking the device:
    private void handleShakeEvent(int count) {
        cancelSearchActions(0);
    } // end method for actions on shake.

    // A method to update some text views or other GUI elements at onCreate():
    private void updateGUIFirst() {
        // To have correct the direction as message above search edit:
        updateSearchMessage();

        // The number of words in DB:
        String sql = "SELECT COUNT(*) FROM dictionar" + direction;
        Cursor cursor = mDbHelper.queryData(sql);
        int totalWords = cursor.getInt(0);
        cursor.close();
        // First take the corresponding plural resource:
        Resources res = getResources();
        String numberOfWordsMessage = res.getQuantityString(R.plurals.tv_number_of_words, totalWords, totalWords);

        // Update the tvStatus TextView:
        TextView tv = findViewById(R.id.tvStatus);
        tv.setText(numberOfWordsMessage);

        // Add an action listener for the keyboard:
        EditText input = findViewById(R.id.etWord);
        input.setInputType(input.getInputType() | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchDirectlyFromKeyboard();
            }
            return false;
        });
        // End add action listener.
    } // end updateGUIFIrst() method.

    // Methods for buttons when searching:
    public void searchButton(View view) {
        getWordFromDB(direction);
    } // end searchButton() method.

    public void searchDirectlyFromKeyboard() {
        if (isImeAction) {
            getWordFromDB(direction);
        }
    } // end search directly from keyboard.

    // A method to get the text filled in the search EditText:
    private String getTextFromEditText() {
        EditText input = findViewById(R.id.etWord);
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
        String text = input.getText().toString();
        // Check if there is something typed there:
        if (text.length() < 1) {
            // Show a warning here if written text is shorter than 2 characters:
            GUITools.alert(this, getString(R.string.warning), getString(R.string.warning_wrong_search));
            SoundPlayer.playSimple(this, "results_not_available");

            return null;
        } else if (text.length() == 1) {
            /*
             * It means is only one character written, we add the start sign to
             * search also the short records:
             */
            text = st.replaceCharacters(text) + "*";
            return text;
        } else {
            // Increase the number of searches:
            searchedWords = searchedWords + 1;
            if (!isPremium) {
                // We check if is a manual registration:
                if (text.equalsIgnoreCase(myUniqueId)) {
                    recreateThisActivityAfterRegistering();
                    return null;
                } // end if is a manual registration.
            } // end if is not premium.

            return st.replaceCharacters(text);
        }
    } // end getTextFromEditText() method.

    // The method to search and show a query:
    private void getWordFromDB(int direction) {
        // Get the string filled in the EditText:
        String word = getTextFromEditText();

        // Only if there is something typed in the EditText:
        if (word != null) {
            // Make now the query string depending if is search middle or
            // not:
            String SQL;

            // Make the SQL query string depending of the search type:
            if (isSearchFullText) {
                SQL = "SELECT *, 1 AS sortare FROM dictionar" + direction + " WHERE termen2='" + word + "' OR termen2 LIKE '" + word + ",%' OR termen2 LIKE '" + word + ".%' OR termen2 LIKE '" + word + " %' UNION SELECT *, 2 AS sortare FROM dictionar" + direction + " WHERE termen2 LIKE '% " + word + "' OR termen2 LIKE '% " + word + ",%' OR termen2 LIKE '% " + word + ".%' OR termen2 LIKE '% " + word + " %' ORDER BY sortare, termen2";
            } else {
                // No full text search:
                SQL = "SELECT *, 1 AS sortare from dictionar" + direction + " WHERE termen2 LIKE '" + word + "%' union SELECT *,2 AS sortare from dictionar" + direction + " WHERE termen2 LIKE '%" + word + "%' AND termen2 NOT LIKE '" + word + "%' ORDER BY sortare, termen2";
            } // end SQL query string for not full text.

            Cursor cursor = mDbHelper.queryData(SQL);
            int type = 0; // word not found.
            // Only if there are results:
            int count = cursor.getCount();
            if (count > 0) {
                type = 1; // word found.
                // Play a specific sound for results shown:
                SoundPlayer.playSimple(this, "results_shown");

                // We hide the bottom layout if is premium:
                if (isPremium) {
                    llBottomInfo.setVisibility(View.GONE);
                }

                // Clear the previous content of the llResult layout:
                llResults.removeAllViews();

                /*
                 * Create a text view for title, announcing the number of
                 * results:
                 */
                // First take the corresponding plural resource:
                Resources res = getResources();
                String foundResults = res.getQuantityString(R.plurals.tv_number_of_results, count, count);
                // Create the number of results text view:
                TextView tvResults = new TextView(this);
                tvResults.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize + 1);
                tvResults.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                tvResults.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
                tvResults.setText(foundResults);
                tvResults.setId(R.id.tvNumberOfResults);
                tvResults.setFocusable(true);
                tvResults.setNextFocusUpId(R.id.btCancelSearch);
                // tvResults.setNextFocusDownId(1000001);
                llResults.addView(tvResults);

                // Create TextViews for each word.
                // We need the string with place holders:
                String tvWordAndExplanation = getString(R.string.tv_word_and_explanation);
                /*
                 * For limit, we have a variable which will be incremented until
                 * resultsLimit:
                 */
                int it = 0;
                int curResultId = 1000001;
                TextView tv;
                cursor.moveToFirst();
                do {
                    tv = new TextView(this);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                    tv.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
                    // w means word, e means explanation:
                    final String w = cursor.getString(0);
                    final String e = cursor.getString(1);
                    String tvText = String.format(tvWordAndExplanation, w, e);
                    CharSequence tvSeq = MyHtml.fromHtml(tvText);
                    tv.setText(tvSeq);
                    tv.setId(curResultId);
                    if (it > 0) {
                        tv.setNextFocusUpId(curResultId - 1);
                    } else {
                        tv.setNextFocusUpId(R.id.tvNumberOfResults);
                    }

                    tv.setNextFocusDownId(++curResultId);
                    tv.setFocusable(true);
                    // For a short click, speak result:
                    tv.setOnClickListener(view -> speakResult(w, e));
                    // End add listener for short click on a result.

                    registerForContextMenu(tv);

                    llResults.addView(tv);

                    it++;
                    if (it >= resultsLimit) {
                        break;
                    }
                } while (cursor.moveToNext());
                // end do ... while.

                // Show the message for more than limit results:
                if (count > resultsLimit) {
                    String moreResultsMessage = String.format(getString(R.string.message_for_more_results), "" + resultsLimit);
                    tv = new TextView(this);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                    tv.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
                    tv.setPadding(mPaddingDP, mPaddingDP * 2, mPaddingDP, mPaddingDP);
                    tv.setText(moreResultsMessage);
                    tv.setNextFocusUpId(curResultId - 1);
                    tv.setId(curResultId);
                    tv.setFocusable(true);
                    llResults.addView(tv);
                } // end show message for more results.
            } // end if there were results in cursor.
            // If there are no results, getCount is 0:
            else {
                showWhenNoResults(word);
            } // end if there were no results.

            // Insert last search into database:
            if (isHistory) {
                searchHistory.addRecord(word, direction, type);
            } // end if search history is activated.
        } // end if there was something typed in the EditText.
    } // end getWordFromDB() method.

    // A method to cancel a search:
    public void cancelSearchButton(View view) {
        cancelSearchActions(0);
    } // end cancelButton method.

    private void cancelSearchActions(int where) {
        // Find the edit text to erase all content:
        EditText et = findViewById(R.id.etWord);
        et.setText("");

        // Show the keyboard for a new search:
        if (where == 0) {
            SoundPlayer.playSimple(this, "results_canceled");
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
        } // end if is from cancelButton or shake action.

        // Erase also the llResults layout:
        llResults.removeAllViews();

        // Show again the llBottomInfo layout:
        if (isPremium) {
            // llBottomInfo.setVisibility(View.VISIBLE);
        }

    } // end cancelSearchActions method.

    // A method to write in the results area that there are no results:
    private void showWhenNoResults(String searchedWord) {

        // Play a corresponding sound if results are not available:
        SoundPlayer.playSimple(this, "results_not_available");

        // Clear the previous content of the llResult layout:
        llResults.removeAllViews();

        // Create a TextView for message no results:
        TextView tv = new TextView(this);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        tv.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
        searchedWord = st.polishString(searchedWord);
        String tvText = String.format(getString(R.string.warning_not_results), searchedWord);
        CharSequence tvSeq = MyHtml.fromHtml(tvText);
        tv.setText(tvSeq);
        tv.setId(R.id.tvNumberOfResults);
        tv.setFocusable(true);
        tv.setNextFocusUpId(R.id.btSearch);
        llResults.addView(tv);
    } // end showWhenNoResults method.

    // A method for switch button:
    public void switchButton(View view) {
        switchDirectionAction();
    } // end switchButton() method.

    // The method which makes the switch:
    private void switchDirectionAction() {
        postStatistics();
        SoundPlayer.playSimple(this, "switch_direction");
        if (direction == 1) {
            // It means it is Romanian English, it will be English Romanian:
            direction = 0;
        } else {
            direction = 1;
        }
        updateGUIFirst();
        // Save the new direction in SharedPreferences:
        Settings set = new Settings(this);
        set.saveIntSettings("direction", direction);
    } // end switchDirectionAction() method.

    // The method which updates the text view for search message:
    private void updateSearchMessage() {
        EditText et = findViewById(R.id.etWord);
        et.setHint(aDirection[direction]);

        // Set also the image with flags for switch button:
        String flagFileName = "flag" + direction;
        ImageButton ib = findViewById(R.id.btSwitch);
        String uri = "@drawable/" + flagFileName;
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        ib.setImageResource(imageResource);

        cancelSearchActions(1);
    } // end updateSearchMessage() method.

    // A method to post statistics:
    private void postStatistics() {
        // Post the statistics and return:
        if (searchedWords > 0) {
            int statsTip = 41 + direction;
            Statistics.postStats("" + statsTip, searchedWords);
            searchedWords = 0;
        }
    } // end postStatistics() method.

    // A method to speak text from a line of results:
    private void speakResult(final String word, final String explanation) {
        // At this moment, speak the English part:
        if (direction == 0) {
            speak.sayUsingLanguage(word, true);
        } // end if is English Romanian variant.
        else {
            speak.sayUsingLanguage(explanation, true);
        } // end if is Romanian English variant.
    } // end speakResult() method.

    // A method to spell text from a line of results:
    private void spellResult(final String word, final String explanation) {
        // At this moment, speak the French part:
        if (direction == 0) {
            speak.spellUsingLanguage(word);
        } // end if is English Romanian variant.
        else {
            speak.spellUsingLanguage(explanation);
        } // end if is Romanian English variant.
    } // end speakResult() method.

    // The method to generate the AdMob sequence:
    /*
    private void adMobSequence() {
        //initializing the Google Admob SDK
        MobileAds.initialize(this, initializationStatus -> {
            // Now, because it is initialized, we load the ad:
            loadBannerAd();
        });
    } // end adMobSequence().

    // Now we will create a simple method to load the Banner Ad inside QuizActivity class as shown below:
    private void loadBannerAd() {
        // Creating  a Ad Request
        AdRequest adRequest = new AdRequest.Builder().build();
        // load Ad with the Request
        bannerAdView.loadAd(adRequest);
    } // end loadBannerAd() method.
// end Google ads section.
*/
    // The finishing of some actions, an overrided method of the activity class:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Now if it's here after speech:
        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String spoken = result.get(0);
                // Now set the etWord:
                EditText et = findViewById(R.id.etWord);
                et.setText(spoken);
                getWordFromDB(direction);
            }
        } // end if it's here after a speech.
    } // end onActivityResult() method.


    // Methods for add to vocabulary:
    private void addToVocabulary(final String word, final String explanation) {
        // Start things for our database vocabulary:
        final DBAdapter2 mDbHelper2 = new DBAdapter2(this);
        mDbHelper2.createDatabase();
        mDbHelper2.open();

        // Reset some values:
        idSection = 0;

        // We create first a layout for this action:
        LinearLayout addLLMain = new LinearLayout(this);
        addLLMain.setOrientation(LinearLayout.VERTICAL);

        // A LayoutParams to add next items into addLLMain:
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView tvAddThis = new TextView(this);
        tvAddThis.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        tvAddThis.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
        // Make the string for this text view:
        String strAddThis = String.format(getString(R.string.tv_add_this_word), String.format(getString(R.string.tv_word_and_explanation), word, explanation));
        tvAddThis.setText(MyHtml.fromHtml(strAddThis));
        addLLMain.addView(tvAddThis, llParams);

        // A radio group for categories:
        final RadioGroup rg = new RadioGroup(this);

        // We need to make here an edit text and check boxes for each category:
        // In categories check boxes will appear also the number of existing
        // words:
        final EditText et = new EditText(this);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        et.setHint(getString(R.string.et_new_vocabulary_hint));
        et.setPadding(mPaddingDP, mPaddingDP * 3, mPaddingDP, mPaddingDP);
        et.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // We uncheck all radio buttons:
                rg.clearCheck();
            } // end if action done was chosen.
            return false;
        });
        // End add action listener for the IME done button of the keyboard..

        addLLMain.addView(et, llParams);

        // In scroll view all the existing categories as check boxes:
        ScrollView sv = new ScrollView(this);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        // Now create the sections as check boxes:
        // First query the existing sections:
        String sql = "SELECT * FROM sectiuni ORDER BY nume COLLATE NOCASE";
        final Cursor cursorSections = mDbHelper2.queryData(sql);

        // A do ... while to create the radio buttons:
        int count = cursorSections.getCount();
        if (count > 0) {
            cursorSections.moveToFirst();
            // Make a RadioGroup:

            rg.setOrientation(RadioGroup.VERTICAL);
            // A LayoutParams to add as match parent the radio buttons:
            RadioGroup.LayoutParams rgParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            Resources res = getResources();
            do {
                RadioButton rbt = new RadioButton(this);
                rbt.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                rbt.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
                // Set the catTitle:
                final int curSection = cursorSections.getInt(0);
                int nrOfWords = getNumberOfWordsInCategory(curSection);
                String catTitle = String.format(getString(R.string.cbt_category), cursorSections.getString(1), res.getQuantityString(R.plurals.number_of_words_in_category, nrOfWords, nrOfWords));
                rbt.setText(MyHtml.fromHtml(catTitle));

                rbt.setOnClickListener(view -> {
                    // Change the id of the chosen section:
                    idSection = curSection;
                    // Empty the edit text because a section was chosen:
                    et.setText("");
                });
                // End add listener for tap on check box.
                rg.addView(rbt, rgParams);
            } while (cursorSections.moveToNext());
            ll.addView(rg);
        } // end if there are sections.

        sv.addView(ll);
        addLLMain.addView(sv);

        // Create now the alert:
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.add_to_vocabulary_alert_title));
        alertDialog.setView(addLLMain);

        // The buttons can be add now and cancel!:
        // end if add now was pressed.
        alertDialog.setPositiveButton(getString(R.string.bt_add_now), (dialog, whichButton) -> {
            // Start the add process:

            // We need the time in seconds:
            final long timeInSeconds = GUITools.getTimeInSeconds();

            /*
             * If there is written something in the edit text, we
             * consider a new category:
             */
            String etText = et.getText().toString();
            if (!etText.equals("")) {
                etText = st.realEscapeString(etText);
                // Check if section doesn't already exist:
                if (!fieldExists("sectiuni", "nume", etText)) {
                    String sql1 = "INSERT INTO sectiuni (nume, descriere, data) VALUES ('" + etText + "', 'none', '" + timeInSeconds + "')";
                    mDbHelper2.insertData(sql1);

                    /*
                     * Post a record into DB Statistics about
                     * section creation:
                     */
                    Statistics.postStats("43", 1);

                } // end if section name doesn't already exists.
                else {
                    GUITools.alert(mFinalContext, getString(R.string.warning), getString(R.string.this_vocabulary_section_already_exists));
                } // end if section name exists.
                /*
                 * After we created this new section, we must
                 * extract the idSection of this:
                 */
                String sql1 = "SELECT id FROM sectiuni WHERE nume='" + etText + "'";
                Cursor tempCursor = mDbHelper2.queryData(sql1);
                idSection = tempCursor.getInt(0);
            } // end if etText was not empty.

            /*
             * Check now if idSection is greater than 0. It means a
             * section was written or it was chosen:
             */
            if (idSection > 0) {

                // Add the word and explanation effectively if
                // record doesn't exists:
                if (!recordExistsInVocabulary(st.realEscapeString(word), st.realEscapeString(explanation))) {
                    String sql1 = "INSERT INTO vocabular (idSectiune, termen, explicatie, data, tip) VALUES ('" + idSection + "', '" + st.realEscapeString(word) + "', '" + st.realEscapeString(explanation) + "', '" + timeInSeconds + "', '" + direction + "')";
                    mDbHelper2.insertData(sql1);
                    SoundPlayer.playSimple(mFinalContext, "hand_writting");

                    /*
                     * Post in statistics about this insert of a
                     * word in DB:
                     */
                    Statistics.postStats("44", 1);
                } // end if record doesn't exist, the best scenario.
                else {
                    GUITools.alert(mFinalContext, getString(R.string.warning), getString(R.string.this_record_already_exists_in_vocabulary));
                } // end if record already exist.
            } else {
                // No section was chosen or written:
                GUITools.alert(mFinalContext, getString(R.string.warning), getString(R.string.no_category_chosen));
            } // end if idSection isn't greater than 0.
            mDbHelper2.close();
        });

        alertDialog.setNegativeButton(getString(R.string.bt_cancel), (dialog, whichButton) -> {
            // Cancelled:
            mDbHelper2.close();
        });

        alertDialog.create();
        alertDialog.show();

    } // end addToVocabulary() method.

    // A method which checks if this entry already exists in DB:
    private boolean recordExistsInVocabulary(String word, String explanation) {
        // This method exists also in VocabularyActivity.
        boolean exists = false;
        DBAdapter2 mDbHelperTemp = new DBAdapter2(this);
        mDbHelperTemp.createDatabase();
        mDbHelperTemp.open();

        String sql = "SELECT COUNT(*) AS total FROM vocabular WHERE termen='" + word + "' AND explicatie='" + explanation + "'";
        Cursor cur = mDbHelperTemp.queryData(sql);
        int count = cur.getInt(0);
        cur.close();
        mDbHelperTemp.close();

        if (count > 0) {
            exists = true;
        }

        return exists;
    } // end recordExists() method.

    // A method which check if a field exists in a database:
    private boolean fieldExists(String table, String field, String text) {
        boolean exists = false;
        DBAdapter2 mDbHelperTemp = new DBAdapter2(this);
        mDbHelperTemp.createDatabase();
        mDbHelperTemp.open();

        String sql = "SELECT COUNT(*) AS total FROM " + table + " WHERE " + field + " = '" + text + "';";
        Cursor cur = mDbHelperTemp.queryData(sql);
        int count = cur.getInt(0);
        cur.close();
        mDbHelperTemp.close();

        if (count > 0) {
            exists = true;
        }

        return exists;
    } // end fieldExists() method.

    // A method which gets the number of words in a category:
    private int getNumberOfWordsInCategory(int id) {
        DBAdapter2 mDbHelperTemp = new DBAdapter2(this);
        mDbHelperTemp.createDatabase();
        mDbHelperTemp.open();

        String sql = "SELECT COUNT(*) AS total FROM vocabular WHERE idSectiune = '" + id + "';";
        Cursor cur = mDbHelperTemp.queryData(sql);
        int count = cur.getInt(0);
        cur.close();
        mDbHelperTemp.close();

        return count;
    } // end getNumberOfWordsInCategory() method.
    // end methods to add to my vocabulary.

    // Methods for voice search:
    public void searchVoiceButton(View view) {
        promptSpeechInput();
    } // end searchVoiceButton() method.

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        // Make different locale for French and Romanian recogniser:
        String[] myLanguages = new String[2];
        myLanguages[0] = "fr";
        myLanguages[1] = "ro";

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, myLanguages[direction]);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, myLanguages[direction]);
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, myLanguages[direction]);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, aSpeechDirection[direction]);
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_LONG).show();
        }
    } // end promptSpeechInput() method.

    // A method to search a word from history:.
    public void searchFromHistory(String word, final int historyDirection) {
        // Find the edit text to put there the word from history:
        EditText et = findViewById(R.id.etWord);
        et.setText(word);

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            // Do something after a while:
            getWordFromDB(historyDirection);
        }, 300);
    } // end searchFromHistory.

    // A method to reset dictionary from menu and also from XML interface:
    public void resetToDefaults() {
        // Get the strings to make an alert:
        String tempTitle = getString(R.string.title_default_settings);
        String tempBody = getString(R.string.body_default_settings);
        new AlertDialog.Builder(this).setTitle(tempTitle).setMessage(tempBody).setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(R.string.yes, (dialog, whichButton) -> {
            Settings set = new Settings(mFinalContext);
            set.setDefaultSettings();
            set.chargeSettings();
            // We must re-initialise also the TTS
            // settings:
            speak = new SpeakText(mFinalContext);
            // We need also to delete the search
            // history:
            searchHistory.deleteSearchHistory();

            /*
             * Get the strings to make an alert for reset
             * the vocabulary:
             */
            String tempTitle1 = getString(R.string.title_default_vocabulary);
            String tempBody1 = getString(R.string.body_default_vocabulary);
            new AlertDialog.Builder(mFinalContext).setTitle(tempTitle1).setMessage(tempBody1).setIcon(android.R.drawable.ic_delete).setPositiveButton(R.string.yes, (dialog1, whichButton1) -> {
                Settings set1 = new Settings(mFinalContext);
                set1.saveIntSettings("db2Ver", 0);
            }).setNegativeButton(R.string.no, null).show();
            // End dialog for delete vocabulary at
            // reset.
        }).setNegativeButton(R.string.no, null).show();
    } // end resetToDefaults() method.


    // Now for billing:
    // Instead In app billing section starts here:
    public void upgradeToPremium(View view) {
        upgradeAlert();
    } // end upgradeToPremium() method.

    public void upgradeAlert() {
        if (GUITools.isNetworkAvailable(this)) {
            ScrollView sv = new ScrollView(this);
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.VERTICAL);
            // The message:
            TextView tv = new TextView(this);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            tv.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
            String message = "";
            if (isPremium) {
                message = getString(R.string.premium_version_alert_message);
            } else {
                // message = String.format(getString(R.string.non_premium_version_alert_message), mUpgradePrice);
            } // end if is not premium.
            tv.setText(message);
            ll.addView(tv);
            // Add the LinearLayout into ScrollView:
            sv.addView(ll);
            // Create now the alert:
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(getString(R.string.premium_version_alert_title));
            alertDialog.setView(sv);
            // The button can be close or Get now!:
            String buttonName;
            if (isPremium) {
                buttonName = getString(R.string.bt_close);
            } else {
                buttonName = getString(R.string.bt_buy_premium);
            }
            alertDialog.setPositiveButton(buttonName, (dialog, whichButton) -> {
                // Start the payment process only if is not premium:
                if (!isPremium) {
                    // upgradeToPremiumActions();
                }
            });
            alertDialog.create();
            alertDialog.show();
        } // end if is connection available.
        else {
            GUITools.alert(this, getString(R.string.warning), getString(R.string.no_connection_available));
        } // end if connection is not available.
    } // end upgradeAlert() method.

    // A method which recreates this activity after upgrading:
    private void recreateThisActivityAfterRegistering() {
        // We save it as an premium version:
        isPremium = true;
        Settings set = new Settings(this);
        set.saveBooleanSettings("isPremium", true);
        // This will go in a meteoric activity and will come back:
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            Intent intent = new Intent(MainActivity.this, PremiumVersionActivity.class);
            startActivity(intent);
            finish();
        });
    } // end recreateThisActivity() method.
    // End methods for InAppBilling.

} // end MainActivity class.
