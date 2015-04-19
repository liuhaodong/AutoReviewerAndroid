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
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

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
//            TextView reviewText = (TextView) findViewById(R.id.review_text);
//            TextView movieNameView = (TextView) findViewById(R.id.movie_name);
            TextView usernameView = (TextView) findViewById(R.id.user_name_result);
            TextView dateView = (TextView) findViewById(R.id.review_date);
//            TextView scoreView = (TextView) findViewById(R.id.score_value);
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

            usernameView.setText(username);
            dateView.setText(date);

            String finalReviewString = "";
            ArrayList<Double> scores = new ArrayList<Double>();

            Map<String, Double> stringScore = new HashMap<>();

            for (int i = 0; i < reviewTextSegments.length; i++) {
//                finalReviewString = finalReviewString + tmp + "\n";
                String parsedTmp[] = reviewTextSegments[i].split(":");
                if (parsedTmp.length < 3){
                    continue;
                } else {
                    Double tmpScore = Double.parseDouble(parsedTmp[2].replace("(", "").replace(")", ""));

                    tmpScore += 0.01*i;

                    stringScore.put(reviewTextSegments[i], tmpScore);
                    scores.add(tmpScore);
                }
            }

            ArrayList<Double> shuffle = new ArrayList<Double>(scores);



            Collections.sort(shuffle);
            ArrayList<Integer>largeThree = new ArrayList<Integer>();
            Double largeSum = 0.0;
            ArrayList<Integer>smallThree = new ArrayList<Integer>();
            Double smallSum = 0.0;
            ArrayList<Integer>midFour = new ArrayList<Integer>();
            Double midSum = 0.0;



            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < scores.size(); j++) {
                    if (scores.get(j) == shuffle.get(i)) {
                        smallThree.add(j);
                        smallSum += scores.get(j);
                    }
                }
                for (int j = 0; j < scores.size(); j++) {
                    if (scores.get(j) == shuffle.get(reviewTextSegments.length - i - 1)) {
                        largeThree.add(j);
                        largeSum += scores.get(j);
                    }
                }
            }

            for (int i = 0; i < scores.size(); i++) {
                boolean flag = true;
                for (int idx : largeThree) {
                    if (i == idx) {
                        flag = false;
                        break;
                    }
                }
                for (int idx : smallThree) {
                    if (i == idx) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    midFour.add(i);
                    midSum += scores.get(i);
                }
            }


            // Large three
            ImageView view1 = (ImageView) findViewById(R.id.imageView);
            view1.setImageResource(getImageResource(largeThree.get(0)));

            ImageView view2 = (ImageView) findViewById(R.id.imageView2);
            view2.setImageResource(getImageResource(largeThree.get(1)));

//            ImageView view3 = (ImageView) findViewById(R.id.imageView3);
//            view3.setImageResource(getImageResource(largeThree.get(2)));

            ((TextView) findViewById(R.id.textView8) ).setText(new DecimalFormat("##.#").format(largeSum.doubleValue()/2) + "");

            // Small three

            ImageView view4 = (ImageView) findViewById(R.id.imageView4);
            view4.setImageResource(getImageResource(smallThree.get(0)));

            ImageView view5 = (ImageView) findViewById(R.id.imageView5);
            view5.setImageResource(getImageResource(smallThree.get(1)));

//            ImageView view6 = (ImageView) findViewById(R.id.imageView6);
//            view6.setImageResource(getImageResource(smallThree.get(2)));

            ((TextView) findViewById(R.id.textView11) ).setText(new DecimalFormat("##.#").format(smallSum.doubleValue()/2) + "");

            // Medium four
            ImageView view7 = (ImageView) findViewById(R.id.imageView7);
            view7.setImageResource(getImageResource(midFour.get(0)));

            ImageView view8 = (ImageView) findViewById(R.id.imageView8);
            view8.setImageResource(getImageResource(midFour.get(1)));

//            ImageView view9 = (ImageView) findViewById(R.id.imageView9);
//            view9.setImageResource(getImageResource(midFour.get(2)));
//
//            ImageView view10 = (ImageView) findViewById(R.id.imageView10);
//            view10.setImageResource(getImageResource(midFour.get(3)));

            ((TextView) findViewById(R.id.textView10) ).setText(new DecimalFormat("##.#").format(midSum.doubleValue()/2) + "");

        }
    }

    private int getImageResource(int index){
        switch (index){
            case 0:
                return R.drawable.p1;
            case 1:
                return R.drawable.p2;
            case 2:
                return R.drawable.p3;
            case 3:
                return R.drawable.p4;
            case 4:
                return R.drawable.p8;
            case 5:
                return R.drawable.p9;
            case 6:
                return R.drawable.p7;
            case 7:
                return R.drawable.p8;
            case 8:
                return R.drawable.p9;
            case 9:
                return R.drawable.p10;
            default:
                return R.drawable.p1;
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
