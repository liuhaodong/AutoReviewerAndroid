package edu.cmu.lti.autoreviewer.autoreviewer;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import edu.cmu.lti.autoreviewer.configuration.DefaultConfig;


public class ResultActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        new ReviewResultReceiver().execute(DefaultConfig.DEFAULT_SERVER_IP, ""+DefaultConfig.DEFAULT_REVIEW_PORT);
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
                Log.d("ReviewSocket", strings[0]);
                Log.d("ReviewSocket", strings[1]);
                this.reviewSocket = new Socket(strings[0], Integer.parseInt(strings[1]));
                out = new PrintWriter(reviewSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(
                        reviewSocket.getInputStream()));

                BufferedReader stdIn = new BufferedReader(
                        new InputStreamReader(System.in));
                String userInput;

                builder = new StringBuilder();
                //System.out.print ("input: ");

                out.println("This is a review request!");

                String line = null;

                while ( (line = in.readLine())!=null ){
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
            TextView reviewText = (TextView) findViewById(R.id.review_text);
            TextView movieNameView = (TextView) findViewById(R.id.movie_name);
            TextView usernameView = (TextView) findViewById(R.id.user_name_result);
            TextView dateView = (TextView) findViewById(R.id.review_date);
            TextView scoreView = (TextView) findViewById(R.id.score_value);
            String[] reviewArray = result.split("#");
            String movieName = reviewArray[0];
            String username = reviewArray[1];
            String date = reviewArray[2];
            String score = reviewArray[3];
            String reviewTextString = reviewArray[4];

            reviewText.setText(reviewTextString);
            movieNameView.setText(movieName);
            usernameView.setText(username);
            dateView.setText(date);
            scoreView.setText(score);
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
