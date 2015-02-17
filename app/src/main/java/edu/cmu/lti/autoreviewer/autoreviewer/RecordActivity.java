package edu.cmu.lti.autoreviewer.autoreviewer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOError;
import java.io.IOException;

import edu.cmu.lti.autoreviewer.helper.EEGDataUploader;
import edu.cmu.lti.autoreviewer.musereceiver.MuseIOReceiver;


public class RecordActivity extends ActionBarActivity implements MuseIOReceiver.MuseDataListener {

    private MuseIOReceiver receiver;

    private EEGDataUploader uploader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        this.receiver = new MuseIOReceiver();
        this.receiver.registerMuseDataListener(this);

        try {
            this.uploader = new EEGDataUploader();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread newThread = new Thread(uploader);
        newThread.start();
//        this.uploader = new EEGDataUploader();

        try {
            this.receiver.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Activity Created!");

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            this.receiver.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        System.out.println(" ALPHA EEG !!!!");

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) RecordActivity.this
                        .findViewById(R.id.eeg_signal)).setText(String.format(
                        "%.2f", alpha[0]));
            }
        });
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
    public void receiveMuseEeg(MuseIOReceiver.MuseConfig config, float[] eeg) {
        this.uploader.addData(eeg, System.currentTimeMillis()/1000L);
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

            resultButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent resultIntent = new Intent(v.getContext(), ResultActivity.class);
                    startActivity(resultIntent);
                }
            });

            return rootView;
        }
    }
}
