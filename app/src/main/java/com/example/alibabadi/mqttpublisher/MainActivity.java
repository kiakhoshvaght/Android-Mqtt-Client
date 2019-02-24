package com.example.alibabadi.mqttpublisher;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.alibabadi.mqttpublisher.helper.MqttHelper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    Button connectBtn;
    LinearLayout connectionStatusLl;
    EditText serverAddressEt;
    EditText portEt;
    EditText messageEt;
    EditText usernameEt;
    EditText passwordEt;
    Button publishBtn;
    MqttHelper mqttHelper;
    String TAG = "MainActivity";
    String[] numbs = {"10", "12", "15", "4", "20", "23", "54", "112", "3", "6"};
    Subscription subscribe;
    Button button;
    EditText topicEt;

    Boolean isConnected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findElementsOfActivity();
        setOnClickListenerOfButtons();
        setUpQaConnection();

    }

    private void publishMsg(String payload) {
        mqttHelper.setTopic(topicEt.getText().toString());
        MqttMessage mqttMessage = new MqttMessage();
        byte[] encodedByte = new byte[0];
        try {
            encodedByte = payload.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mqttMessage.setPayload(encodedByte);
        mqttHelper.publishData(mqttMessage);
    }

    private String getRandomNumber() {
        Random r = new Random();
        int Low = 5;
        int High = 200;
        int result = r.nextInt(High - Low) + Low;
        return String.valueOf(result);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (subscribe != null) {
            if (!subscribe.isUnsubscribed()) {
                subscribe.unsubscribe();
            }
        }
        super.onDestroy();
    }

    private void findElementsOfActivity() {
        publishBtn = findViewById(R.id.publish_btn);
        serverAddressEt = findViewById(R.id.server_address_et);
        portEt = findViewById(R.id.port_et);
        messageEt = findViewById(R.id.message_et);
        connectionStatusLl = findViewById(R.id.connection_status_ll);
        connectBtn = findViewById(R.id.connect_btn);
        usernameEt = findViewById(R.id.username_et);
        passwordEt = findViewById(R.id.password_et);
        topicEt = findViewById(R.id.topic_et);

        setElementsOfDisconnectedState();
    }

    private void setOnClickListenerOfButtons() {

        publishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishMsg(messageEt.getText().toString());
            }
        });

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"FUNCTION : setOnClickListenerOfButtons => onClick of connectBtn");
                if (!isConnected) {
                    Log.i(TAG,"FUNCTION : setOnClickListenerOfButtons => onClick of connectBtn => Is not connected");
                    if (!serverAddressEt.getText().toString().equals("") && !portEt.getText().toString().equals("") &&
                        !usernameEt.getText().toString().equals("") && !passwordEt.getText().toString().equals("")) {
                        Log.i(TAG,"FUNCTION : setOnClickListenerOfButtons => onClick of connectBtn => Is not connected => Text fields are not empty");
                        mqttHelper = new MqttHelper(getApplicationContext());
                        mqttHelper.setServerURI(serverAddressEt.getText().toString() + ":" + passwordEt.getText().toString());
                        mqttHelper.setUserName(usernameEt.getText().toString());
                        mqttHelper.setPasswd(passwordEt.getText().toString());
                        setMqttCallBack();
                        mqttHelper.connect().subscribe(new Subscriber<Boolean>() {
                            @Override
                            public void onCompleted() {
                                Log.i(TAG, "FUNCTION : setOnClickListenerOfButtons => Connecting to mqtt => onCompleted.");
                                setElementsOfConnectedState();
                                isConnected = true;
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "FUNCTION : setOnClickListenerOfButtons => Connecting to mqtt => onError => " + e.toString());
                            }

                            @Override
                            public void onNext(Boolean aBoolean) {
                                Log.i(TAG, "FUNCTION : setOnClickListenerOfButtons => Connecting to mqtt => onNext.");
                            }
                        });
                    }
                } else {
                    mqttHelper.disconnect();
                    mqttHelper = null;
                    setElementsOfDisconnectedState();
                }
            }
        });
    }

    private void setElementsOfConnectedState() {
        serverAddressEt.setEnabled(false);
        portEt.setEnabled(false);
        usernameEt.setEnabled(false);
        passwordEt.setEnabled(false);
        serverAddressEt.setBackgroundColor(getResources().getColor(R.color.gray_btn_bg_pressed_color));
        usernameEt.setBackgroundColor(getResources().getColor(R.color.gray_btn_bg_pressed_color));
        passwordEt.setBackgroundColor(getResources().getColor(R.color.gray_btn_bg_pressed_color));
        portEt.setBackgroundColor(getResources().getColor(R.color.gray_btn_bg_pressed_color));

        topicEt.setEnabled(true);
        publishBtn.setEnabled(true);
        messageEt.setEnabled(true);

        messageEt.setBackgroundColor(getResources().getColor(R.color.White));
        topicEt.setBackgroundColor(getResources().getColor(R.color.White));

        connectBtn.setText("Disconnect");

        connectionStatusLl.setBackgroundColor(getResources().getColor(R.color.Green));
    }

    private void setElementsOfDisconnectedState() {
        serverAddressEt.setEnabled(true);
        portEt.setEnabled(true);
        usernameEt.setEnabled(true);
        passwordEt.setEnabled(true);
        serverAddressEt.setBackgroundColor(getResources().getColor(R.color.white));
        usernameEt.setBackgroundColor(getResources().getColor(R.color.white));
        passwordEt.setBackgroundColor(getResources().getColor(R.color.white));
        portEt.setBackgroundColor(getResources().getColor(R.color.white));

        messageEt.setEnabled(false);
        publishBtn.setEnabled(false);
        topicEt.setEnabled(false);

        connectionStatusLl.setBackgroundColor(getResources().getColor(R.color.Red));

        topicEt.setBackgroundColor(getResources().getColor(R.color.gray_btn_bg_pressed_color));
        messageEt.setBackgroundColor(getResources().getColor(R.color.gray_btn_bg_pressed_color));

        connectBtn.setText("Connect");
    }

    private void setMqttCallBack() {
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.e(TAG, "Connected " + serverURI);

            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.e(TAG, "LOST");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    private void setUpQaConnection(){
        serverAddressEt.setText("tcp://172.20.1.29");
        portEt.setText("1883");
        usernameEt.setText("PASSENGER");
        passwordEt.setText("PASSENGER");
        topicEt.setText("client/event/drv/test");
        messageEt.setText("{\t\n" +
                "  \"eventType\" : \"message\" ,\n" +
                "  \"eventInfo\" : \"PLEASE_WAIT\"\n" +
                "}");
    }
}
