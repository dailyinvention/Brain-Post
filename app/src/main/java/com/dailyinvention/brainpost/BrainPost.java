package com.dailyinvention.brainpost;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class BrainPost extends Activity {
	BluetoothAdapter bluetoothAdapter;

	TextView tv;
	Button b;

	TGDevice tgDevice;
	final boolean rawEnabled = false;

    InputStream is = null;

    private WebSocketClient connectSocketClient;
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
        connectWebSocket();
        tv = findViewById(R.id.textView1);
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
            URI uri;
            JSONObject brainJSON = new JSONObject();
            try {
                switch (msg.what) {
                    case TGDevice.MSG_STATE_CHANGE:
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
                        brainJSON.put("heartrate", Integer.toString(msg.arg1));
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
                    brainJSON.put("meditation", Integer.toString(meditation));
                    brainJSON.put("attention", Integer.toString(attention));
                    brainJSON.put("theta", Integer.toString(theta));
                    brainJSON.put("delta", Integer.toString(delta));
                    brainJSON.put("alpha", Integer.toString(alpha));
                    brainJSON.put("beta", Integer.toString(beta));
                    brainJSON.put("gamma", Integer.toString(gamma));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            connectSocketClient.send(brainJSON.toString());
        }
    };

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://192.168.1.136:8080");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        connectSocketClient = new WebSocketClient(uri) {

            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
            }

            @Override
            public void onMessage(String s) {
                Log.i("Websocket", s);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        connectSocketClient.connect();
    }

    public void doStuff(View view) {
    	if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
    		tgDevice.connect(rawEnabled);
    	//tgDevice.ena
    }
}