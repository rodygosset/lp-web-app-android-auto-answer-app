package com.example.rap.Fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.example.rap.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class SendFragment extends Fragment implements View.OnClickListener {

    // data retrieved from the other fragments
    String selectedAnswer;
    ArrayList<String> contactNames;

    // UI components

    Button sendButton;
    CheckBox autoAnswer;
    TextView selectedContactsView;
    TextView selectedAnswerView;

    Bundle savedState;

    // needed to retrieve the phone numbers from the

    Cursor contactsCursor;

    // save the app context

    Activity app;

    // used to run the auto answer service
    Intent autoAnswerServiceIntent;

    public SendFragment() {
        savedState = new Bundle();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View result = inflater.inflate(R.layout.send_fragment, container, false);

        // load previous state if available

        Bundle args = getArguments();
        if(args != null) {
            contactNames = args.getStringArrayList("SELECTED_CONTACTS");
            selectedAnswer = args.getString("SELECTED_ANSWER");
        }


        // listen for contact list updates

        getParentFragmentManager().setFragmentResultListener("CONTACT_LIST_UPDATE", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                // get the list of contacts
                contactNames = result.getStringArrayList("CONTACT_LIST");
                if(contactNames.size() > 0 && selectedContactsView != null) {
                    String contactList = String.join(", ", contactNames);
                    selectedContactsView.setText(contactList);
                }
                // save the current state
                savedState.putStringArrayList("SELECTED_CONTACTS", contactNames);
                setArguments(savedState);
                // update the auto answer service
                if(autoAnswer != null && autoAnswer.isChecked()) {
                    updateAutoAnswerService();
                }
            }
        });

        // listen for selected answer updates

        getParentFragmentManager().setFragmentResultListener("SELECTED_ANSWER_UPDATE", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                // get the list
                selectedAnswer = result.getString("SELECTED_ANSWER", "No answer selected");
                if(selectedAnswerView != null) {
                    selectedAnswerView.setText(selectedAnswer != null ? selectedAnswer : "No answer selected");
                }
                // save the current state
                savedState.putString("SELECTED_ANSWER", selectedAnswer);
                setArguments(savedState);
                // update the auto answer service
                if(autoAnswer != null && autoAnswer.isChecked()) {
                    updateAutoAnswerService();
                }
            }
        });


        return result;
    }


    private void updateAutoAnswerService() {
        stopAutoAnswerService();
        startAutoAnswerService();
    }

    // when starting the auto answer service
    private void startAutoAnswerService() {
        // always provide it with the list of contacts and the answer
        autoAnswerServiceIntent.putExtra("ANSWER", selectedAnswer != null ? selectedAnswer : "I'm busy...");
        autoAnswerServiceIntent.putExtra("NUMBERS", getAllPhoneNumbers());
        // run it
        app.startService(autoAnswerServiceIntent);
    }

    private void stopAutoAnswerService() {
        app.stopService(autoAnswerServiceIntent);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
        super.onViewCreated(view,savedInstance);
        // get the views
        sendButton = (Button) view.findViewById(R.id.send_button);
        autoAnswer = (CheckBox) view.findViewById(R.id.auto_checkbox);
        selectedContactsView = (TextView) view.findViewById(R.id.selected_contacts);
        selectedAnswerView = (TextView) view.findViewById(R.id.selected_answer);
        sendButton.setOnClickListener(this);

        // keep UI in sync with state

        if(selectedAnswerView != null) {
            selectedAnswerView.setText(selectedAnswer != null ? selectedAnswer : "No answer selected");
        }

        // update the text view containing the list of contacts

        if(contactNames != null && contactNames.size() > 0 && selectedContactsView != null) {
            String contactList = String.join(", ", contactNames);
            selectedContactsView.setText(contactList);
        }


        // stop & start auto answer service depending on autoAnswer checkbox

        // init service intent

        app = getActivity();
        if(app == null) return;
        autoAnswerServiceIntent = new Intent(app, AutoAnswerSMSService.class);

        // on click logic for the checkbox

        autoAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(autoAnswer.isChecked()) {
                    startAutoAnswerService();
                } else {
                    stopAutoAnswerService();
                }
            }
        });

        // init cursor

        contactsCursor = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null);
    }

    // send the selected answer to the provided phone number

    public void sendSms(String phoneNumber) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber,null,selectedAnswer,null,null);
            // notify the user on success
            Toast.makeText(getActivity(), "Message Sent",Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // let the user know if there was a problem
            Toast.makeText(getActivity(), e.getMessage(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    // given a contact name,
    // retrieve their phone number
    public String getContactPhoneNumber(String contactName) {
        int phoneColIdx = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        int nameColIdx = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        contactsCursor.moveToFirst();
        // for each contact
        while(contactsCursor.moveToNext()) {
            String name = contactsCursor.getString(nameColIdx);
            String phoneNumber = contactsCursor.getString(phoneColIdx);
            // if the name matches,
            // return the phone number
            if(contactName.equals(name)) {
                return phoneNumber;
            }
        }
        return null;
    }

    // build an array list containing the list of phone numbers to auto answer

    public ArrayList<String> getAllPhoneNumbers() {
        ArrayList<String> numbers = new ArrayList<>();
        // for each contact
        for(String contactName : contactNames) {
            // get their phone number
            String phoneNumber = getContactPhoneNumber(contactName);
            if(phoneNumber != null) {
                // add it to the array list
                numbers.add(phoneNumber);
            }
        }
        return numbers;
    }

    // send button onclick handler

    @Override
    public void onClick(View view) {
        // for each selected contact
        for(String contactName : contactNames) {
            // get their phone number
            String phoneNumber = getContactPhoneNumber(contactName);
            if(phoneNumber != null) {
                // and send the selected message
                sendSms(phoneNumber);
            }
        }
    }

}
