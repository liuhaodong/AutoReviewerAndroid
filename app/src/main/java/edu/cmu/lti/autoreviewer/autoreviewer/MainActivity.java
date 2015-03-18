package edu.cmu.lti.autoreviewer.autoreviewer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;

import edu.cmu.lti.autoreviewer.configuration.DefaultConfig;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            SharedPreferences sharedPref = getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, 0);
            String username = sharedPref.getString(getString(R.string.prompt_username), DefaultConfig.DEFAULT_USERNAME);
            String serverIP = sharedPref.getString(getString(R.string.prompt_server_ip), DefaultConfig.DEFAULT_SERVER_IP);
            String[] fakeData = {"La Luna","Iphone ADs","Different Phones", "Transformers","Godzilla"};

            ArrayAdapter<String> movieAdapter = new ArrayAdapter<String>(getActivity(), R.layout.movie_text, R.id.movie_text,  Arrays.asList(fakeData));

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            final ListView movieList = (ListView) rootView.findViewById(R.id.movie_selection_list);
            movieList.setAdapter(movieAdapter);


            movieList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent recordIntent = new Intent(getActivity(), RecordActivity.class);
                    String movieName = movieList.getItemAtPosition(position).toString();
                    switch (movieName){
                        case "La Luna":
                            recordIntent.putExtra("MovieName", "La Luna");
                            break;
                        case "Iphone ADs":
                            recordIntent.putExtra("MovieName", "iphone_ads");
                            break;
                        case "Different Phones":
                            recordIntent.putExtra("MovieName", "iphone_android_wp");
                            break;
                        default:
                            recordIntent.putExtra("MovieName", movieList.getItemAtPosition(position).toString().trim());
                            break;
                    }
//                    recordIntent.putExtra("MovieName", movieList.getItemAtPosition(position).toString().trim());
                    startActivity(recordIntent);
                }
            });


            return rootView;
        }
    }
}
