package edu.cmu.lti.autoreviewer.helper;

import android.content.SharedPreferences;
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

import edu.cmu.lti.autoreviewer.autoreviewer.LoginActivity;
import edu.cmu.lti.autoreviewer.autoreviewer.R;
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

    private String username;
    private String serverIP;

    public EEGDataUploader(String pUsername, String pServerIP) throws IOException {
        this.ch0 = new ArrayList<Float>();
        this.ch1 = new ArrayList<Float>();
        this.ch2 = new ArrayList<Float>();
        this.ch3 = new ArrayList<Float>();
        this.timer = System.currentTimeMillis() / 1000L;
        this.username = pUsername;
        this.serverIP = pServerIP;
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
                eegUploadData.append(username.hashCode());
                eegUploadData.append(",");
                eegUploadData.append(username);
                eegUploadData.append(",");
                //System.out.println("debug: "+eegData.toString());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();

                String dateString = dateFormat.format(date);

                eegUploadData.append(dateString);
                eegUploadData.append(",");
                //System.out.println("debug: "+eegData.toString());
                date.setTime(date.getTime() + 1000);

                dateString = dateFormat.format(date);
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
//                Log.d("EEGDATA", eegUploadData.toString());
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
            uploadSocket = new Socket(serverIP, DefaultConfig.DEFAULT_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
