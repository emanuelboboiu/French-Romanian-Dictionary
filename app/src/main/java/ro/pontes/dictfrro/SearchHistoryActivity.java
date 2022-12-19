package ro.pontes.dictfrro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class SearchHistoryActivity extends Activity implements
        OnItemSelectedListener {

    private SearchHistory searchHistory;
    private final Context mFinalContext = this;
    private SpeakText speak;

    private int curCategory = 0; // it's selected in combo above.
    private int type = 1;
    private int direction = 0;
    private String orderBy = "data DESC";

    private StringTools st;

    /*
     * We need a global variable TextView for click on a result. A value will be
     * attributed in onCreateContextMenu, and will be read in
     * onContextItemSelected:
     */
    private TextView tvResultForContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_history);

        speak = new SpeakText(this);
        st = new StringTools(this);
        searchHistory = new SearchHistory(this);

        Spinner dropdown = findViewById(R.id.spinnerChooseSort);
        dropdown.setOnItemSelectedListener(this);

        setBottomStatus();
        showResults(curCategory); // default value is 0.
    }

    private void setBottomStatus() {
        TextView tv = findViewById(R.id.tvStatus);
        int nrOfSearches = searchHistory.getNumberOfRecords();
        if (MainActivity.isHistory) {
            Resources res = getResources();
            String bottomStatusMessage = res
                    .getQuantityString(R.plurals.tv_number_of_searches,
                            nrOfSearches, nrOfSearches);
            tv.setText(bottomStatusMessage);
        } // end if is search history activated.
        else {
            String bottomStatusMessage = getString(R.string.sh_is_disabled);
            tv.setText(bottomStatusMessage);
        } // end if search history is not activated.

        // If no searches were done, disable the delete history button:
        if (nrOfSearches == 0) {
            ImageButton ib = findViewById(R.id.btDeleteHistory);
            ib.setEnabled(false);
        } // end if no searches were done, disable the delete history button.
    } // end setBottomStatus() method.

    // A method to fill the scroll view with searched words:
    public void showResults(int category) {
        // First set the combo above to the curCategory item:
        Spinner dropdown = findViewById(R.id.spinnerChooseSort);
        dropdown.setSelection(curCategory);

        int mPaddingDP = MainActivity.mPaddingDP;
        int textSize = MainActivity.textSize;

        // Clear the previous content of the llResult layout:
        LinearLayout llResults = findViewById(R.id.llResults);
        llResults.removeAllViews();

        Cursor cursor = searchHistory.getSearchesCursor(type, direction,
                orderBy);
        if (cursor != null) {

            // Make a LayoutParams to match parent horizontally:
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            // Create TextViews for each word.
            // We need the string with place holders:
            String tvSearchedWord = getString(R.string.tv_searched_word);
            TextView tv;
            int it = 0;
            int curResultId = 1000001;
            cursor.moveToFirst();
            do {
                tv = new TextView(this);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                tv.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
                tv.setGravity(Gravity.LEFT);
                // w means word, d means date, id = the ID in database:
                final String w = cursor.getString(4);
                final String d = GUITools.timeStampToString(this,
                        cursor.getInt(5));
                String tvText = String.format(tvSearchedWord, w, d);
                CharSequence tvSeq = MyHtml.fromHtml(tvText);
                tv.setText(tvSeq);
                int id = cursor.getInt(0);
                tv.setTag(id);
                tv.setId(curResultId);
                if (it > 0) {
                    tv.setNextFocusUpId(curResultId - 1);
                } else {
                    tv.setNextFocusUpId(R.id.spinnerChooseSort);
                }
                tv.setNextFocusDownId(++curResultId);
                tv.setFocusable(true);

                // For a short click, speak result:
                tv.setOnClickListener(view -> {
                    if (direction == 0) {
                        // Only for English words:
                        speak.sayUsingLanguage(w, false);
                    } else {
                        GUITools.alert(
                                mFinalContext,
                                getString(R.string.warning),
                                getString(R.string.no_romanian_language_to_speak));
                    } // end if Romanian results, no speak.
                });
                // End add listener for short click on a result.

                registerForContextMenu(tv);

                llResults.addView(tv, tvParams);
                it++;
            } while (cursor.moveToNext());
            // end do ... while.
        } // end if there where results.

        else {

            // Nor results found in history:
            TextView tv;
            tv = new TextView(this);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            tv.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
            String tvText = getString(R.string.tv_no_results_found_in_history);
            CharSequence tvSeq = MyHtml.fromHtml(tvText);
            tv.setText(tvSeq);
            llResults.addView(tv);

        } // end if no results where found in history.
    }// end showResults() method.

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.go_to_dictionary) {
            this.finish();
            GUITools.goToDictionary(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        // Shut down also the TTS:
        speak.close();
        super.onDestroy();
    } // end onDestroy method.

    @Override
    public void onBackPressed() {
        this.finish();
        GUITools.goToDictionary(this);
    } // end onBackPressed()

    public void deleteSearchHistory(View view) {
        String tempTitle = getString(R.string.sh_title_delete_history);
        String tempBody = getString(R.string.sh_body_delete_history);
        new AlertDialog.Builder(this)
                .setTitle(tempTitle)
                .setMessage(tempBody)
                .setIcon(android.R.drawable.ic_delete)
                .setPositiveButton(R.string.yes,
                        (dialog, whichButton) -> {
                            searchHistory.deleteSearchHistory();
                            // Play a delete sound:
                            SoundPlayer.playSimple(mFinalContext,
                                    "delete_history");

                            // After delete, set again the status:
                            setBottomStatus();
                            // show results again, no results found:
                            showResults(curCategory);
                        }).setNegativeButton(R.string.no, null).show();
    } // end deleteSearchHistory() method.

    public void deleteWordHistory(final int id) {
        String tempTitle = getString(R.string.sh_title_delete_word_history);
        String tempBody = getString(R.string.sh_body_delete_word_history);
        new AlertDialog.Builder(this)
                .setTitle(tempTitle)
                .setMessage(tempBody)
                .setIcon(android.R.drawable.ic_delete)
                .setPositiveButton(R.string.yes,
                        (dialog, whichButton) -> {
                            searchHistory.deleteWordFromHistory(id);
                            // Play a delete sound:
                            SoundPlayer.playSimple(mFinalContext,
                                    "delete_history");

                            // After delete, set again the status:
                            setBottomStatus();
                            // show results again:
                            showResults(curCategory);
                        }).setNegativeButton(R.string.no, null).show();
    } // end deleteSearchHistory() method.

    // For context menu:
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(getString(R.string.cm_result_title));
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_history_context_menu, menu);

        // We store globally the text view clicked longly:
        tvResultForContext = (TextView) v;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // First we take the text from the longly clicked result:
        String result = tvResultForContext.getText().toString();
        String[] aResult = result
                .split(""
                        + MyHtml.fromHtml(getString(R.string.sh_word_and_date_separator)));
        String w = st.cleanString(aResult[0]);

        // We take also the id of the word in database:
        int wordId = (Integer) tvResultForContext.getTag();
        @SuppressWarnings("unused")
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();
        switch (item.getItemId()) {
            case R.id.cmSearchResult:
                goToDictionaryAndSearch(w, direction);
                return true;

            case R.id.cmSpeakResult:
                speak.sayUsingLanguage(w, false);
                return true;

            case R.id.cmSpellResult:
                speak.spellUsingLanguage(w);
                return true;

            case R.id.cmCopyResult:
                GUITools.copyIntoClipboard(this, w);
                return true;

            case R.id.cmDeleteResult:
                deleteWordHistory(wordId);
                return true;

            default:
                return super.onContextItemSelected(item);
        } // end switch.
    } // End context menu implementation.

    // end for context menu things.
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {

        curCategory = position; // a global variable.

        switch (position) {

            case 0:
                direction = 0;
                type = 1;
                orderBy = "termen ASC";

                break;
            case 1:
                direction = 0;
                type = 1;
                orderBy = "data DESC";

                break;
            case 2:
                direction = 0;
                type = 0;
                orderBy = "termen ASC";

                break;
            case 3:
                direction = 0;
                type = 0;
                orderBy = "data DESC";

                break;
            case 4:
                direction = 1;
                type = 1;
                orderBy = "termen ASC";

                break;
            case 5:
                direction = 1;
                type = 1;
                orderBy = "data DESC";

                break;
            case 6:
                direction = 1;
                type = 0;
                orderBy = "termen ASC";

                break;
            case 7:
                direction = 1;
                type = 0;
                orderBy = "data DESC";

                break;
        } // end switch(position).

        showResults(curCategory);
    } // end onItemSelected() method.

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    } // end onNothingSelected() method.

    // A method to go into dictionary to search current word:
    public void goToDictionaryAndSearch(String word, int direction) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("wordFromHistory", word + direction);
        startActivity(intent);
        this.finish();
    } // end goToDictionary.

} // end SearchHistoryActivity class.
