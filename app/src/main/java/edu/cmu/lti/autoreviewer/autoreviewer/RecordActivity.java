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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOError;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.cmu.lti.autoreviewer.configuration.DefaultConfig;
import edu.cmu.lti.autoreviewer.helper.EEGDataUploader;
import edu.cmu.lti.autoreviewer.musereceiver.MuseIOReceiver;


public class RecordActivity extends ActionBarActivity implements MuseIOReceiver.MuseDataListener {

    private MuseIOReceiver receiver;

    private EEGDataUploader uploader;

    public static boolean uploadFlag = false;

    public static String movieName;

    public static Date startTime;
    public static Date endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        Intent myIntent = getIntent();

        movieName = myIntent.getStringExtra("MovieName");

        this.receiver = new MuseIOReceiver();
        this.receiver.registerMuseDataListener(this);

        SharedPreferences sharedPref = this.getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        String username = sharedPref.getString(getString(R.string.prompt_username), DefaultConfig.DEFAULT_USERNAME);
        String serverIP = sharedPref.getString(getString(R.string.prompt_server_ip), DefaultConfig.DEFAULT_SERVER_IP);


        try {
            this.uploader = new EEGDataUploader(username, serverIP);
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onResume();
        Thread newThread = new Thread(uploader);
        newThread.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
            AsyncTask newTask = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    try {
                        RecordActivity.this.receiver.connect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };

        newTask.execute();

    }

    @Override
    protected void onPause() {
        super.onPause();
        this.receiver.disconnect();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record, menu);
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

    @Override
    public void receiveMuseElementsAlpha(MuseIOReceiver.MuseConfig config, final float[] alpha) {

    }

    @Override
    public void receiveMuseElementsBeta(MuseIOReceiver.MuseConfig config, float[] beta) {

    }

    @Override
    public void receiveMuseElementsTheta(MuseIOReceiver.MuseConfig config, float[] theta) {

    }

    @Override
    public void receiveMuseElementsDelta(MuseIOReceiver.MuseConfig config, float[] delta) {

    }

    @Override
    public void receiveMuseEeg(MuseIOReceiver.MuseConfig config, final float[] eeg) {



        if (RecordActivity.uploadFlag) {
            Log.d("Record", "Recording");
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView eegValueView = (TextView) findViewById(R.id.eeg_value);
                    String eegValueString = "";
                    for(int i=0; i<eeg.length; i++){
                        eegValueString+=(int) eeg[i];
                        eegValueString+="   ";
                    }
                    eegValueView.setText(eegValueString);
                }
            });
            try {
                this.uploader.addData(eeg, System.currentTimeMillis() / 1000L);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void receiveMuseAccel(MuseIOReceiver.MuseConfig config, float[] accel) {

    }

    @Override
    public void receiveMuseBattery(MuseIOReceiver.MuseConfig config, int[] battery) {

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
            View rootView = inflater.inflate(R.layout.fragment_record, container, false);

            Button resultButton = (Button) rootView.findViewById(R.id.get_result_button);

            ImageView img= (ImageView) rootView.findViewById(R.id.movie_image);


            switch (movieName){
                case "La Luna":
                    img.setImageResource(R.drawable.la_luna);
                    break;
                case "Transformers":
                    img.setImageResource(R.drawable.transformers);
                    break;
                case "Godzilla":
                    img.setImageResource(R.drawable.godzilla);
                    break;
                default:
                    break;
            }



            resultButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent resultIntent = new Intent(v.getContext(), ResultActivity.class);
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String startTime = df.format(RecordActivity.startTime);
                    String endTime = df.format(RecordActivity.endTime);
                    resultIntent.putExtra(getString(R.string.start_time), startTime);
                    resultIntent.putExtra(getString(R.string.end_time), endTime);
                    resultIntent.putExtra("MovieName", RecordActivity.movieName);
                    startActivity(resultIntent);
                }
            });


            final Button recordButton = (Button) rootView.findViewById(R.id.record_button);
            recordButton.setTag(1);
            recordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int status = (Integer) v.getTag();
                    if (status == 1) {
                        RecordActivity.startTime = new Date();

                        recordButton.setText("Stop");
                        v.setTag(0); //pause
                        RecordActivity.uploadFlag = !RecordActivity.uploadFlag;
                    } else {
                        RecordActivity.endTime = new Date();
                        recordButton.setText("Record");
                        v.setTag(1); //record
                        RecordActivity.uploadFlag = !RecordActivity.uploadFlag;
                    }
                }
            });


            return rootView;
        }
    }
}
