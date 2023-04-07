package com.example.rap.Fragments;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class AutoAnswerSMSService extends Service {

    private String answer;
    private ArrayList<String> phoneNumbers;

    // auto answer received messages
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        // when we recieve a text message
        @Override
        public void onReceive(Context context, Intent intent) {
            // retrieve the message
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            String phoneNumber = messages[messages.length - 1].getOriginatingAddress();
            // if it was sent by one of the selected users
            if(phoneNumbers.contains(phoneNumber)) {
                // auto answer it
                sendSms(phoneNumber);
            }
        }
    };


    // everytime the service starts
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // retrieve the phone numbers of the selected contacts
        phoneNumbers = intent.getStringArrayListExtra("NUMBERS");
        // and the selected message
        answer = intent.getStringExtra("ANSWER");
        // start the message receiver
        IntentFilter filter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(messageReceiver, filter);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // cleanup

    @Override
    public void onDestroy() {
        unregisterReceiver(messageReceiver);
    }


    // send the saved answer to the provided phone number

    private void sendSms(String phoneNumber) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber,null, answer,null,null);
            // on success, notify the user
            Toast.makeText(getApplicationContext(), "Auto Answered Message",Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // let the user know in case there's a problem
            // by making a toast
            Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

}
