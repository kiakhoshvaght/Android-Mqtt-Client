package com.example.alibabadi.mqttpublisher.helper;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import rx.Observable;
import rx.Subscriber;

public class MqttHelper {
    private String TAG = "MQTT_HELPER";

    private MqttAndroidClient androidClient;

    private String serverURI = "tcp://172.20.1.29:1883";

    public String getServerURI() {
        return serverURI;
    }

    public void setServerURI(String serverURI) {
        this.serverURI = serverURI;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    private String clientId = "9174cd97-e904-477d-891f-98ebe875ce57";
    private String topic = "test/";

    private String userName = "PASSENGER";
    private String passwd = "PASSENGER";

    public MqttHelper(Context context) {
        androidClient = new MqttAndroidClient(context, serverURI, clientId);
        androidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public void setCallback(MqttCallbackExtended callbackExtended) {
        androidClient.setCallback(callbackExtended);
    }

    public Observable<Boolean> connect() {
        Log.i(TAG,"FUNCTION : connect");
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> observer) {
                Log.i(TAG,"FUNCTION : connect => call");
                MqttConnectOptions options = new MqttConnectOptions();
                options.setAutomaticReconnect(true);
                options.setCleanSession(false);
                options.setUserName(userName);
                options.setMaxInflight(1000);
                options.setPassword(passwd.toCharArray());
                try {
                    androidClient.connect(options, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.e(TAG, "FUNCTION : connect => onSuccess");
                            DisconnectedBufferOptions bufferOptions = new DisconnectedBufferOptions();
                            bufferOptions.setBufferEnabled(true);
                            bufferOptions.setBufferSize(100);
                            bufferOptions.setPersistBuffer(false);
                            bufferOptions.setDeleteOldestMessages(false);
                            androidClient.setBufferOpts(bufferOptions);
                            observer.onCompleted();
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.e(TAG, "FUNCTION : connect => " + exception.toString() + " ");
                            observer.onError(exception);
                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void publishData(MqttMessage mqttMessage) {
        try {
            androidClient.publish(topic, mqttMessage, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e(TAG, "Published " + asyncActionToken.getResponse().toString());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "FAILED " + exception.toString());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    public void onDisposeMqtt() {
        try {
            if (androidClient.isConnected()) {
                androidClient.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(){
        try {
            androidClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
