package ro.pontes.dictfrro;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

/*
 * Started on 21 June 2014, at 19:10 by Manu.
 * This class has some useful things for the GUI, like alerts.
 */

public class GUITools {

    // A method to go to dictionary activity:
    public static void goToDictionary(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    } // end go to dictionary activity.

    // A method to go to vocabulary activity:
    public static void goToVocabulary(Context context) {
        Intent intent = new Intent(context, VocabularyActivity.class);
        context.startActivity(intent);
    } // end go to vocabulary activity.

    // A method to go to history activity:
    public static void goToHistory(Context context) {
        Intent intent = new Intent(context, SearchHistoryActivity.class);
        context.startActivity(intent);
    } // end go to SearchHistoryActivity.

    // A method to show an alert with title and message, just an OK button:
    public static void alert(Context context, String title, String message) {

        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        // The title:
        alert.setTitle(title);

        // The body creation:
        // Create a LinearLayout with ScrollView with all contents as TextViews:
        ScrollView sv = new ScrollView(context);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        String[] mParagraphs = message.split("\n");

        // A for for each paragraph in the message as TextView:
        for (String mParagraph : mParagraphs) {
            TextView tv = new TextView(context);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
            tv.setText(mParagraph);
            ll.addView(tv);
        } // end for.

        // Add now the LinearLayout into ScrollView:
        sv.addView(ll);

        alert.setView(sv);

        alert.setPositiveButton("Ok", (dialog, whichButton) -> {
            // Do nothing yet...
        });
        alert.show();
    } // end alert static method.

    // A method for about dialog for this package:
    @SuppressLint("InflateParams")
    public static void aboutDialog(Context context) {
        // Inflate the about message contents
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View messageView = inflater.inflate(R.layout.about_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // builder.setIcon(R.drawable.app_icon);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.setPositiveButton("OK", null);
        builder.create();
        builder.show();
    } // end about dialog.

    // A method to play a tone, just to make tests:
    public static void beep() {
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
    }

    // A method to open the browser with an URL:
    private static final String HTTPS = "https://";
    private static final String HTTP = "http://";

    public static void openBrowser(final Context context, String url) {

        if (!url.startsWith(HTTP) && !url.startsWith(HTTPS)) {
            url = HTTP + url;
        }

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    } // end start browser with an URL in it.

    // For formating a date:
    public static String timeStampToString(Context context, int paramCurTime) {
        long curTime = (long) paramCurTime * 1000;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(curTime);

        // Now format the string:
        // See if it is today or yesterday:
        int today = getIsToday(curTime);
        String dayOfWeek;
        if (today == 1) {
            dayOfWeek = context.getString(R.string.today);
        } else if (today == 2) {
            dayOfWeek = context.getString(R.string.yesterday);
        } else {
            dayOfWeek = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        }

        // Make the hour and minute with 0 in front if they are less
        // than 10:
        String curHour;
        int iHour = cal.get(Calendar.HOUR_OF_DAY);
        if (iHour < 10) {
            curHour = "0" + iHour;
        } else {
            curHour = "" + iHour;
        }
        String curMinute;
        int iMinute = cal.get(Calendar.MINUTE);
        if (iMinute < 10) {
            curMinute = "0" + iMinute;
        } else {
            curMinute = "" + iMinute;
        }

        return String.format(context.getString(R.string.date_format), dayOfWeek, "" + cal.get(Calendar.DAY_OF_MONTH), "" + cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()), "" + cal.get(Calendar.YEAR), curHour, curMinute);
    } // end timeStampToString() method.

    /*
     * This method returns 1 if a date in milliseconds at parameter is today, 2
     * if it was yesterday or 0 on another date.
     */
    public static int getIsToday(long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis);

        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return 1; // today.
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return 2; // yesterday.
        } else if (smsTime.get(Calendar.DATE) - now.get(Calendar.DATE) == 1) {
            return 3; // tomorrow.
        } else {
            return 0; // another date.
        }
    } // end determine if a date is today or yesterday.

    // A static method to get a random number between two integers:
    public static int random(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    } // end random method.

    // A method to go to web page of the application:
    public static void goToAppWebPage(final Context context) {

        String url = "http://www.android.pontes.ro/frd/index.php?lang=";

        // Determine if is RO at the start of the current language:
        // Get the system current locale: // Get the locale:
        String curLocale = context.getResources().getConfiguration().locale.getDisplayName();
        curLocale = curLocale.substring(0, 2);
        curLocale = curLocale.toLowerCase(Locale.getDefault());
        if (curLocale.equals("ro")) {
            url += "ro";
        } else {
            url += "en";
        }

        // Call now the openBrowser method():
        openBrowser(context, url);
    } // end goToAppWebPage() method.

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    } // end isNetworkAvailable() method.

    public static void copyIntoClipboard(final Context context, String text) {
        SoundPlayer.playSimple(context, "copy_into_clipboard");
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Result", text);
        clipboard.setPrimaryClip(clip);
    } // end copyIntoClipboard() method.

    // A method to show help in an alert LinearLayout:
    public static void showHelp(final Context context) {
        // Create a LinearLayout with ScrollView with all contents as TextViews:
        ScrollView sv = new ScrollView(context);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        // Get the items to be shown in alert:
        Resources res = context.getResources();
        String[] aInformation = res.getStringArray(R.array.information_array);

        // A for for each message in the history as TextView:
        for (String s : aInformation) {
            TextView tv = new TextView(context);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
            tv.setPadding(MainActivity.mPaddingDP, MainActivity.mPaddingDP, MainActivity.mPaddingDP, MainActivity.mPaddingDP);
            tv.setText(s);
            ll.addView(tv);
        } // end for.
        sv.addView(ll);

        // Create now the alert:
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(context.getString(R.string.help_alert_title));
        alertDialog.setView(sv);
        alertDialog.setPositiveButton(context.getString(R.string.bt_close), null);
        AlertDialog alert = alertDialog.create();
        alert.show();
    } // end showHelp() method.

    // A method to round a double value:
    public static double round(double number, int decimals) {
        return Math.round(number * 100.0) / 100.0;
    } // end round() method.

    // A method to get current time in seconds:
    public static long getTimeInSeconds() {
        Calendar cal = Calendar.getInstance();
        long timeInMilliseconds = cal.getTimeInMillis();
        return timeInMilliseconds / 1000;
    } // end getTimeInSeconds() method.

    // A method to rate this application:
    public static void showRateDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle(context.getString(R.string.title_rate_app)).setMessage(context.getString(R.string.body_rate_app)).setPositiveButton(context.getString(R.string.bt_rate), (dialog, which) -> {
            Settings set = new Settings(context);
            set.saveBooleanSettings("wasRated", true);
            String link = "market://details?id=";
            try {
                // play market available
                context.getPackageManager().getPackageInfo("com.android.vending", 0);
                // not available
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                // Should use browser
                link = "https://play.google.com/store/apps/details?id=";
            }
            // Starts external action
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link + context.getPackageName())));
        }).setNegativeButton(context.getString(R.string.bt_not_now), null);
        builder.show();
    } // end showRateDialog() method.

    // A method which checks if was rated:
    public static void checkIfRated(Context context) {
        Settings set = new Settings(context);
        boolean wasRated = set.getBooleanSettings("wasRated");
        if (!wasRated) {

            if (MainActivity.numberOfLaunches % 6 == 0 && MainActivity.numberOfLaunches > 0) {
                GUITools.showRateDialog(context);
            } // end if was x launches.
        } // end if it was not rated.
    } // end checkIfRated() method.

    // A method which checks if user was noticed about premium version:
    public static void checkIfNoticedAboutPremium(Context context) {
        // only if is not already the premium version:
        if (!MainActivity.isPremium) {
            Settings set = new Settings(context);
            boolean wasNoticedPremium = set.getBooleanSettings("wasNoticedPremium");
            if (!wasNoticedPremium) {
                if (MainActivity.numberOfLaunches % 15 == 0 && MainActivity.numberOfLaunches > 0) {
                    GUITools.alert(context, context.getString(R.string.information), String.format(context.getString(R.string.info_about_premium_version), MainActivity.mUpgradePrice));
                    set.saveBooleanSettings("wasNoticedPremium", true);
                } // end if was the x launch.
            } // end if it was not noticed.
        } // end if is not premium version.
    } // end checkIfNoticedAboutPremium() method.

    // A method to open the app in Play Store:
    public static void openAppInPlayStore(Context context) {
        final String appPackageName = context.getPackageName();
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    } // end openAppInPlayStore() method.

    // A method to set background and other global things about a layout:
    public static void setLayoutInitial(Context context, int layoutType) {
        String mBackground;
        if (MainActivity.background == null || MainActivity.background.equals("")) {
            /*
             * It means no background was chosen and saved, we choose a random
             * one:
             */
            /*
             * Number of backgrounds, in the drawable folder. This number is
             * also specified in BacgroundsActivity:
             */
            int nrOfBackgrounds = 5;
            int curBackgroundNumber = GUITools.random(1, nrOfBackgrounds);
            mBackground = "paper" + curBackgroundNumber;
            // We save the one chosen by random:
            MainActivity.background = mBackground;
            Settings set = new Settings(context);
            set.saveStringSettings("background", MainActivity.background);
        } else {
            /* It means is was saved, we get it from the static String variable: */
            mBackground = MainActivity.background;
        } // end if a background was chosen.

        // Determine the background ID:
        int resId = 0;
        if (!MainActivity.background.equals("paper0")) {
            resId = context.getResources().getIdentifier(mBackground, "drawable", context.getPackageName());
        }

        /* layoutType 1 means relative, 2 means linear. */
        // if is a relative layout:
        if (layoutType == 1) {
            RelativeLayout rl = ((Activity) context).findViewById(R.id.layoutMain);
            rl.setBackgroundResource(resId);
        } // end if layoutType is RelativeLayout.
        // Now for LinearLayout:
        else if (layoutType == 2) {
            LinearLayout ll = ((Activity) context).findViewById(R.id.layoutMain);
            ll.setBackgroundResource(resId);
        } // end if is a LinearLayout.
    } // end setLayoutInitial() method.

} // end GUITools class.
