package com.example.rap.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rap.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class AnswersFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    // instance state

    ArrayList<String> answers;

    String selectedAnswer;

    // last
    ListView answersList;
    Button addButton, deleteButton;
    EditText input;

    SharedPreferences sharedPref;

    // custom adapter

    private class MyArrayAdapter extends ArrayAdapter<String> {

        private LayoutInflater inflater;

        public MyArrayAdapter(Context ctx, int layoutID, ArrayList<String> data) {
            super(ctx, layoutID, data);
            inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null) {
                v = inflater.inflate(R.layout.answers_list_item, null);
            }
            TextView answerText = (TextView) v.findViewById(R.id.answer_text);
            if(answerText == null) return null;
            answerText.setText(answers.get(position));
            CheckBox cBox = (CheckBox) v.findViewById(R.id.checkBox);
            if(cBox != null) {
                // only check the box if the current answer is the selected answer
                cBox.setChecked(selectedAnswer != null && selectedAnswer.equals(answers.get(position)));
            }
            return v;
        }
    }

    MyArrayAdapter adapter;

    public AnswersFragment() {
        answers = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args != null) {
            selectedAnswer = args.getString("SELECTED_ANSWER");
        }
        return inflater.inflate(R.layout.answers_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
        super.onViewCreated(view,savedInstance);
        // retrieve saved answers
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String[] answersArray = sharedPref.getStringSet("answers", new HashSet<String>()).toArray(new String[0]);
        answers = new ArrayList<>(Arrays.asList(answersArray));
        answers.removeAll(Arrays.asList("", null));
        // get the views
        answersList = (ListView) view.findViewById(R.id.answers_list);
        addButton = (Button) view.findViewById(R.id.add_button);
        deleteButton = (Button) view.findViewById(R.id.delete_button);
        input = (EditText) view.findViewById(R.id.input);
        addButton.setOnClickListener(this);
        // instantiate the adapter
        adapter = new MyArrayAdapter(
                getActivity(),
                R.layout.answers_list_item,
                answers);
        // bind the adapter to the list view
        answersList.setAdapter(adapter);
        answersList.setOnItemClickListener(this);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedAnswer == null) return;
                // delete selected element
                adapter.remove(selectedAnswer);
                selectedAnswer = null;
                notifyOtherFragments();
                saveAnswers();
            }
        });
    }

    private void saveAnswers() {
        // save current list to local storage
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("answers", new HashSet<String>(answers));
        editor.apply();
    }

    // when the user clicks on the button

    @Override
    public void onClick(View view) {
        String inputText = input.getText().toString();
        if(inputText.length() == 0 || answers.contains(inputText)) return;
        adapter.add(inputText);
        input.setText("");
        saveAnswers();
    }

    private void notifyOtherFragments() {
        // notify the other fragments of the change
        Bundle bundle = new Bundle();
        bundle.putString("SELECTED_ANSWER", selectedAnswer);
        getParentFragmentManager().setFragmentResult("SELECTED_ANSWER_UPDATE", bundle);
        // save the current state
        setArguments(bundle);
    }

    // when the user clicks on a list item

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowID) {
        String currentAnswer = adapter.getItem(position);
        CheckBox cBox = null;
        // if a message was previously selected
        if(selectedAnswer != null) {
            // uncheck it and return if it's the same as the one that was clicked on
            cBox = answersList.getChildAt(answers.indexOf(selectedAnswer)).findViewById(R.id.checkBox);
            if(cBox == null) return;
            cBox.setChecked(false);
            if(selectedAnswer.equals(currentAnswer)) {
                selectedAnswer = null;
                notifyOtherFragments();
                return;
            }
        }
        cBox = answersList.getChildAt(position).findViewById(R.id.checkBox);
        if(cBox == null) return;
        cBox.setChecked(!cBox.isChecked());
        if(cBox.isChecked()) selectedAnswer = currentAnswer;
        notifyOtherFragments();
    }
}
