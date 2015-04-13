package edu.cmu.lti.autoreviewer.helper;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import com.interaxon.libmuse.Eeg;
import com.interaxon.libmuse.MuseArtifactPacket;
import com.interaxon.libmuse.MuseDataListener;
import com.interaxon.libmuse.MuseDataPacket;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import edu.cmu.lti.autoreviewer.autoreviewer.R;
import edu.cmu.lti.autoreviewer.autoreviewer.RecordActivity;
import edu.cmu.lti.autoreviewer.musereceiver.MuseIOReceiver;

/**
 * Created by haodongl on 3/26/15.
 */
public class DataListener extends MuseDataListener {

    final WeakReference<Activity> activityRef;

    public DataListener(final WeakReference<Activity> activityRef) {
        this.activityRef = activityRef;
    }

    @Override
    public void receiveMuseDataPacket(MuseDataPacket museDataPacket) {
        switch (museDataPacket.getPacketType()) {
            case EEG:
                updateEeg(museDataPacket.getValues());
                break;
            default:
                break;
        }
    }

    private void updateEeg(final ArrayList<Double> data){
        final Activity activity = activityRef.get();
        if (activity != null && ((RecordActivity) activity).uploadFlag ) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView eegValueView = (TextView) activity.findViewById(R.id.eeg_value);
                    String eegValueString = "";
                    eegValueString += String.format(
                            "%.2f", data.get(Eeg.TP9.ordinal())) + "   ";
                    eegValueString += String.format(
                            "%.2f", data.get(Eeg.FP1.ordinal())) + "   ";
                    eegValueString += String.format(
                            "%.2f", data.get(Eeg.FP2.ordinal())) + "   ";
                    eegValueString += String.format(
                            "%.2f", data.get(Eeg.TP10.ordinal()));

                    if(eegValueView instanceof TextView ){
                        eegValueView.setText(eegValueString);
                    }



                    float[] eegData = new float[data.size()];
                    for(int i=0 ;i < data.size() ; i++){
                        eegData[i] = data.get(i).floatValue();
                    }
                    try {
                        ((RecordActivity)activity).uploader.addData(eegData , System.currentTimeMillis() / 1000L);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    @Override
    public void receiveMuseArtifactPacket(MuseArtifactPacket museArtifactPacket) {

    }
}
