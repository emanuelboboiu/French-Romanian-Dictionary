package ro.pontes.dictfrro;

/*
 * Class started on 24 September 2014 by Manu
 * Methods for statistics, like postStatistics.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Statistics {

    // A method to post a new game and the number of hands played during the
    // sessions:
/*
    public static void postStats(final String gameIdInDB,
                                 final int numberOfGamesPlayed) {

        // Run in another thread:
        new Thread(new Runnable() {
            public void run() {

                // Create a new HttpClient and Post Header
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(
                        "http://www.pontes.ro/ro/divertisment/games/soft_counts.php");

                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                            2);
                    nameValuePairs
                            .add(new BasicNameValuePair("pid", gameIdInDB));
                    nameValuePairs.add(new BasicNameValuePair("score", ""
                            + numberOfGamesPlayed));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = httpClient.execute(httppost);
                    response.toString();

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
            }
        }).start();
    } // end post data.

    public static void postTestFinished(final String googleId,
                                        final String testType, final double mark) {

        // Run in another thread:
        new Thread(new Runnable() {
            public void run() {

                // Create a new HttpClient and Post Header
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(
                        "http://android.pontes.ro/erd/insert_test_finished.php");

                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                            3);
                    nameValuePairs.add(new BasicNameValuePair("google_id",
                            googleId));
                    nameValuePairs.add(new BasicNameValuePair("tip", testType));
                    nameValuePairs
                            .add(new BasicNameValuePair("nota", "" + mark));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = httpClient.execute(httppost);
                    response.toString();

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
            }
        }).start();
    } // end post data for a test finished.


    // A method to change the name for mark statistics, tests finished:
    public static void postNewName(final String googleId, final String newName) {

        // Run in another thread:
        new Thread(new Runnable() {
            public void run() {

                // Create a new HttpClient and Post Header
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(
                        "http://android.pontes.ro/erd/change_name.php");

                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                            2);
                    nameValuePairs.add(new BasicNameValuePair("google_id",
                            googleId));
                    nameValuePairs.add(new BasicNameValuePair("nume", newName));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = httpClient.execute(httppost);
                    response.toString();

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
            }
        }).start();
    } // end post data.
*/
} // end statistics class.
