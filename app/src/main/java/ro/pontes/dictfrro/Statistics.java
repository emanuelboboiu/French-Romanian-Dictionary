package ro.pontes.dictfrro;

/*
 * Class started on 24 September 2014 by Manu
 * Rewritten on Tuesday, 21 December 2022.
 * Methods for statistics, like postStatistics.
 */

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Statistics {
    private static final String baseUrl = "https://www.pontes.ro/ro/divertisment/games/soft_counts.php";

    public static void postStats(final String pid, final int score) {
        String url = baseUrl + "?pid=" + pid + "&score=" + score;
        new GetWebData().execute(url);
    } // end postStats() method.


    // This is a subclass:
    private static class GetWebData extends AsyncTask<String, String, String> {

        // execute before task:
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Execute task
        String urlText = "";

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder content = new StringBuilder();
            urlText = strings[0];
            try {
                // Create a URL object:
                URL url = new URL(urlText);
                // Create a URLConnection object:
                URLConnection urlConnection = url.openConnection();
                // Wrap the URLConnection in a BufferedReader:
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                String line;
                // Read from the URLConnection via the BufferedReader:
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line);
                }
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return content.toString();
        } // end doInBackground() method.

        // Execute after task with the task result as string:
        @Override
        protected void onPostExecute(String s) {
            // Do nothing yet.
        } // end postExecute() method.
    } // end subclass.

} // end statistics class.
