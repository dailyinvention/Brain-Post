package com.dailyinvention.brainpost;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;
import com.neurosky.thinkgear.TGEegPower;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BrainPost extends Activity {
	BluetoothAdapter bluetoothAdapter;

	TextView tv;
	Button b;

	TGDevice tgDevice;
	final boolean rawEnabled = false;

    InputStream is = null;
    String connection = "http://192.168.1.136:8080/api/neurobrainpost";
    int heartrate;
    List<NameValuePair> nameValuePairs = new CopyOnWriteArrayList<>();
    private AsyncTask PostBrain;

    int alpha;
    int beta;
    int gamma;
    int delta;
    int theta;
    int meditation;
    int attention;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tv = (TextView)findViewById(R.id.textView1);
        tv.setText("");
        //tv.append("Android version: " + Integer.valueOf(android.os.Build.VERSION.SDK) + "\n" );
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
        	// Alert user that Bluetooth is not available
        	Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
        	finish();
        }else {
        	/* create the TGDevice */
        	tgDevice = new TGDevice(bluetoothAdapter, handler);

        }
    }

    @Override
    public void onDestroy() {
    	tgDevice.close();
        super.onDestroy();
    }

    /**
     * Handles messages from TGDevice
     */
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TGDevice.MSG_STATE_CHANGE:
                    nameValuePairs.clear();
                    switch (msg.arg1) {
                        case TGDevice.STATE_IDLE:
                            break;
                        case TGDevice.STATE_CONNECTING:
                            tv.append("Connecting...\n");
                            break;
                        case TGDevice.STATE_CONNECTED:
                            tv.append("Connected.\n");
                            tgDevice.start();
                            break;
                        case TGDevice.STATE_NOT_FOUND:
                            tv.append("Can't find\n");
                            break;
                        case TGDevice.STATE_NOT_PAIRED:
                            tv.append("not paired\n");
                            break;
                        case TGDevice.STATE_DISCONNECTED:
                            tv.append("Disconnected mang\n");
                    }
                    break;
                case TGDevice.MSG_POOR_SIGNAL:
                    //signal = msg.arg1;
                    tv.append("PoorSignal: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_HEART_RATE:
                    tv.append("Heart rate: " + msg.arg1 + "\n");
                    nameValuePairs.add(new BasicNameValuePair("heartrate", Integer.toString(msg.arg1)));
                    break;
                case TGDevice.MSG_ATTENTION:
                    //att = msg.arg1;
                    tv.append("Attention: " + msg.arg1 + "\n");
                    attention = msg.arg1;
                    //Log.v("HelloA", "Attention: " + att + "\n");
                    break;
                case TGDevice.MSG_MEDITATION:
                    tv.append("Mediation: " + msg.arg1 + "\n");
                    meditation = msg.arg1;
                    break;
                case TGDevice.MSG_BLINK:
                    tv.append("Blink: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_LOW_BATTERY:
                    Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
                    break;
                case TGDevice.MSG_EEG_POWER:
                    TGEegPower eegPower = (TGEegPower) msg.obj;
                    tv.append("Delta: " + eegPower.delta +
                            "\nTheta: " + eegPower.theta +
                            "\nAlpha1: " + eegPower.lowAlpha +
                            "\nAlpha2: " + eegPower.highAlpha +
                            "\nBeta1: " + eegPower.lowBeta +
                            "\nBeta2: " + eegPower.highBeta +
                            "\nGamma1: " + eegPower.lowGamma +
                            "\nGamma2: " + eegPower.midGamma + "\n\n");
                    alpha = (eegPower.lowAlpha + eegPower.highAlpha) / 2;
                    beta = (eegPower.lowBeta + eegPower.highBeta) / 2;
                    gamma = (eegPower.lowGamma + eegPower.midGamma) / 2;
                    delta = eegPower.delta;
                    theta = eegPower.theta;
                    break;
                default:
                    break;
            }

            nameValuePairs.add(new BasicNameValuePair("meditation", Integer.toString(meditation)));
            nameValuePairs.add(new BasicNameValuePair("attention", Integer.toString(attention)));
            nameValuePairs.add(new BasicNameValuePair("theta", Integer.toString(theta)));
            nameValuePairs.add(new BasicNameValuePair("delta", Integer.toString(delta)));
            nameValuePairs.add(new BasicNameValuePair("alpha", Integer.toString(alpha)));
            nameValuePairs.add(new BasicNameValuePair("beta", Integer.toString(beta)));
            nameValuePairs.add(new BasicNameValuePair("gamma", Integer.toString(gamma)));

            HttpClient httpclient = HttpClientBuilder.create().build();

            HttpPost httpPost = new HttpPost(connection);
            HttpResponse response;

            httpPost.setHeader("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                httpclient.execute(httpPost);

                //Get the response


                response = httpclient.execute(httpPost);

                int responseCode = response.getStatusLine().getStatusCode();
                String responseText = Integer.toString(responseCode);
                Log.i("HTTP POST:", "HTTP POST : " + responseText);

                /*Checking response */

                InputStream in = response.getEntity().getContent(); //Get the data in the entity
                Log.i("HTTP RESPONSE", "HTTP POST : " + in.toString());

                //Print result
                Log.i("HTTP Result:", response.toString());

            } catch (IOException e) {

                Log.e("HTTP Error:", e.toString());
            }

            nameValuePairs.clear();
        }
    };

    public void doStuff(View view) {
    	if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
    		tgDevice.connect(rawEnabled);
    	//tgDevice.ena
    }
}