package edu.cmu.lti.autoreviewer.autoreviewer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import edu.cmu.lti.autoreviewer.configuration.DefaultConfig;


public class ResultActivity extends ActionBarActivity {

    public static String username;
    public static String serverIP;

    public static String startTime;
    public static String endTime;
    public static String movieName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        SharedPreferences sharedPref = this.getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        username = sharedPref.getString(getString(R.string.prompt_username), DefaultConfig.DEFAULT_USERNAME);
        String serverIP = sharedPref.getString(getString(R.string.prompt_server_ip), DefaultConfig.DEFAULT_SERVER_IP);

        Intent myIntent = getIntent();

        startTime = myIntent.getStringExtra(getString(R.string.start_time));
        endTime = myIntent.getStringExtra(getString(R.string.end_time));
        movieName = myIntent.getStringExtra("MovieName");
        new ReviewResultReceiver().execute(serverIP, "" + DefaultConfig.DEFAULT_REVIEW_PORT);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class ReviewResultReceiver extends AsyncTask<String, Integer, String> {

        Socket reviewSocket;
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder builder = null;

        @Override
        protected String doInBackground(String... strings) {
            try {
                this.reviewSocket = new Socket(strings[0], Integer.parseInt(strings[1]));
                out = new PrintWriter(reviewSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(
                        reviewSocket.getInputStream()));

                BufferedReader stdIn = new BufferedReader(
                        new InputStreamReader(System.in));
                String userInput;

                builder = new StringBuilder();
                //System.out.print ("input: ");

                String outputString = "start: " + ResultActivity.startTime + "#end: " + ResultActivity.endTime + "#movie: " + movieName + "#subjectName: " + username + "#subjectId: " + username.hashCode();

                Log.d("requestString", outputString);
//                out.println("start: 2015-03-14 09:05:21#end: 2015-03-14 09:12:01#movie: La Luna#subjectName: test#subjectId: 1");

                out.println(outputString);

                String line = null;

                while ((line = in.readLine()) != null) {
                    builder.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "No Review!";
            }
            String tmp = builder.toString();
            return tmp;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("review", result);
            TextView reviewText = (TextView) findViewById(R.id.review_text);
            TextView movieNameView = (TextView) findViewById(R.id.movie_name);
            TextView usernameView = (TextView) findViewById(R.id.user_name_result);
            TextView dateView = (TextView) findViewById(R.id.review_date);
            TextView scoreView = (TextView) findViewById(R.id.score_value);
            String[] reviewArray = result.split("#");
            if (reviewArray.length < 5) {
                return;
            }
            String movieName = reviewArray[0];
            String username = reviewArray[1];
            String date = reviewArray[2];
            String score = reviewArray[3];
            String reviewTextString = reviewArray[4];
            int timeInterval = Integer.parseInt(reviewArray[5]);

            String[] rawDataString = reviewArray[6].split(" ");

            String[] reviewTextSegments = reviewTextString.split("\\$");

            String finalReviewString = "";

            for (String tmp : reviewTextSegments) {
                finalReviewString = finalReviewString + tmp + "\n";
            }

            reviewText.setText(finalReviewString);
            movieNameView.setText(movieName);
            usernameView.setText(username);
            dateView.setText(date);
            scoreView.setText(score+"/10");

            GraphView graph = (GraphView) findViewById(R.id.result_graph);
            DataPoint[] dataPoints = new DataPoint[rawDataString.length];

            int sum = 0;
            for (int i = 0; i < rawDataString.length; i++) {
                dataPoints[i] = new DataPoint(i * timeInterval, Double.parseDouble(rawDataString[i]));
            }

            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);

            graph.addSeries(series);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_result, container, false);

            return rootView;
        }
    }
}
