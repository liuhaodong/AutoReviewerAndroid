package edu.cmu.lti.autoreviewer.autoreviewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseConnectionListener;
import com.interaxon.libmuse.MuseConnectionPacket;
import com.interaxon.libmuse.MuseManager;
import com.interaxon.libmuse.MusePreset;
import com.interaxon.libmuse.MuseVersion;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.autoreviewer.helper.MuseSingle;


public class MuseConnect extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private Muse muse = null;
    private ConnectionListener connectionListener = null;
    private boolean dataTransmission = true;

    public static ConnectionState currentStatus = null;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View v) {
        {
            Log.d("connect","button clicked");
            Spinner musesSpinner = (Spinner) findViewById(R.id.connect_spinner);
            if (v.getId() == R.id.connect_refresh) {
                MuseManager.refreshPairedMuses();
                List<Muse> pairedMuses = MuseManager.getPairedMuses();
                List<String> spinnerItems = new ArrayList<String>();
                for (Muse m : pairedMuses) {
                    String dev_id = m.getName() + "-" + m.getMacAddress();
                    spinnerItems.add(dev_id);
                }
                ArrayAdapter<String> adapterArray = new ArrayAdapter<String>(
                        this, android.R.layout.simple_spinner_item, spinnerItems);
                musesSpinner.setAdapter(adapterArray);
            } else if (v.getId() == R.id.connect_connect) {
                List<Muse> pairedMuses = MuseManager.getPairedMuses();
                if (pairedMuses.size() < 1 ||
                        musesSpinner.getAdapter().getCount() < 1) {
                    Log.w("Muse Headband", "There is nothing to connect to");
                } else {
                    muse = pairedMuses.get(musesSpinner.getSelectedItemPosition());
                    MuseSingle.setMuse(muse);
                    ConnectionState state = muse.getConnectionState();
                    if (state == ConnectionState.CONNECTED ||
                            state == ConnectionState.CONNECTING) {
                        return;
                    }
                    configure_library();
                    /**
                     * In most cases libmuse native library takes care about
                     * exceptions and recovery mechanism, but native code still
                     * may throw in some unexpected situations (like bad bluetooth
                     * connection). Print all exceptions here.
                     */
                    try {
                        muse.runAsynchronously();
                    } catch (Exception e) {
                        Log.e("Muse Headband", e.toString());
                    }
                }
            } else if (v.getId() == R.id.connect_next) {



                if(MuseConnect.currentStatus != ConnectionState.CONNECTED){
                    new AlertDialog.Builder(this)
                            .setTitle("Muse Not Connected")
                            .setMessage("Please Connect Muse First")
                            .setCancelable(false).setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, close
                            // current activity
                            dialog.cancel();
                        }
                    })
                            .create().show();
                    return;
                }

                Intent mainIntent = new Intent(this.getBaseContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
            }

        }
    }

    class ConnectionListener extends MuseConnectionListener {

        final WeakReference<Activity> activityRef;

        ConnectionListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseConnectionPacket(MuseConnectionPacket p) {
            final ConnectionState current = p.getCurrentConnectionState();
            MuseConnect.currentStatus = current;
            final String status = p.getPreviousConnectionState().toString() +
                    " -> " + current;
            final String full = "Muse " + p.getSource().getMacAddress() +
                    " " + status;
            Log.i("Muse Headband", full);
            Activity activity = activityRef.get();
            // UI thread is used here only because we need to update
            // TextView values. You don't have to use another thread, unless
            // you want to run disconnect() or connect() from connection packet
            // handler. In this case creating another thread is required.
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView statusText =
                                (TextView) findViewById(R.id.connect_status);
                        statusText.setText(status);

                        if (current == ConnectionState.CONNECTED) {
                            MuseVersion museVersion = muse.getMuseVersion();
                            String version = museVersion.getFirmwareType() +
                                    " - " + museVersion.getFirmwareVersion() +
                                    " - " + Integer.toString(
                                    museVersion.getProtocolVersion());
                        } else {
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muse_connect);

        WeakReference<Activity> weakActivity =
                new WeakReference<Activity>(this);
        connectionListener = new ConnectionListener(weakActivity);

        Button refreshButton = (Button) findViewById(R.id.connect_refresh);
        refreshButton.setOnClickListener(this);
        Button connectButton = (Button) findViewById(R.id.connect_connect);
        connectButton.setOnClickListener(this);
        Button nextButton = (Button) findViewById(R.id.connect_next);
        nextButton.setOnClickListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_muse_connect, menu);
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

    private void configure_library() {
        muse.registerConnectionListener(connectionListener);
        muse.setPreset(MusePreset.PRESET_14);
        muse.enableDataTransmission(dataTransmission);
    }

}
