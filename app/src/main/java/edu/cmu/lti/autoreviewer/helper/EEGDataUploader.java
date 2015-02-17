package edu.cmu.lti.autoreviewer.helper;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.cmu.lti.autoreviewer.configuration.DefaultConfig;

/**
 * Created by haodongl on 2/16/15.
 */
public class EEGDataUploader implements Runnable {

    private Socket uploadSocket;

    private List<Float> ch0;
    private List<Float> ch1;
    private List<Float> ch2;
    private List<Float> ch3;

    private long timer;

    private boolean startUpload = false;

    public EEGDataUploader() throws IOException {
        this.ch0 = new ArrayList<Float>();
        this.ch1 = new ArrayList<Float>();
        this.ch2 = new ArrayList<Float>();
        this.ch3 = new ArrayList<Float>();
        this.timer = System.currentTimeMillis() / 1000L;

    }


    public void addData(final float[] eegData, final long newTimer) throws IOException {
        if ( newTimer - timer < 1 || !startUpload) {
            ch0.add(eegData[0]);
            ch1.add(eegData[1]);
            ch2.add(eegData[2]);
            ch3.add(eegData[3]);
            startUpload = true;
        } else {
            // Upload data

            PrintWriter out = new PrintWriter(uploadSocket.getOutputStream(), true);

            for (int i = 0; i < 4; i++) {
                StringBuilder eegUploadData = new StringBuilder();
                eegUploadData.append(1);
                eegUploadData.append(",");
                eegUploadData.append(DefaultConfig.DEFAULT_USERNAME);
                eegUploadData.append(",");
                //System.out.println("debug: "+eegData.toString());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Calendar cal = Calendar.getInstance();

                String dateString = dateFormat.format(cal.getTime());

                eegUploadData.append(dateString);
                eegUploadData.append(",");
                //System.out.println("debug: "+eegData.toString());
                cal.add(Calendar.SECOND, 1);
                dateString = dateFormat.format(cal.getTime());
                eegUploadData.append(dateString);

                eegUploadData.append(",");
                //System.out.println("debug: "+eegData.toString());

                eegUploadData.append("ch" + i);
                eegUploadData.append(",");

                List<Float> eegList = this.getChannel(i);
                if(eegList.isEmpty()){
                    break;
                }
                for (Float tmp : eegList) {
                    eegUploadData.append((int)tmp.floatValue());
                    eegUploadData.append(" ");
                }

                out.println(eegUploadData.toString());
                Log.d("EEGDATA", eegUploadData.toString());
                eegList.clear();

            }
            this.timer = newTimer;

        }


    }


    private List<Float> getChannel(int index) {
        if (index == 0) {
            return this.ch0;
        } else if (index == 1) {
            return this.ch1;
        } else if (index == 2) {
            return this.ch2;
        } else if (index == 3) {
            return this.ch3;
        } else {
            return null;
        }
    }


    @Override
    public void run() {
        try {
            uploadSocket = new Socket(DefaultConfig.DEFAULT_SERVER_IP, DefaultConfig.DEFAULT_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
