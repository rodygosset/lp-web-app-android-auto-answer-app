package com.example.rap.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;


import com.example.rap.MainActivity;
import com.example.rap.R;

import java.util.ArrayList;

public class ContactsFragment extends Fragment implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {


    // instance attributes

    ListView contactsList;

    ArrayList<String> selectedContactNames;

    // from the Android documentation =>

    /*
     * Defines an array that contains column names to move from
     * the Cursor to the ListView.
     */
    @SuppressLint("InlinedApi")
    private final static String[] FROM_COLUMNS = {
            Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                    ContactsContract.Contacts.DISPLAY_NAME
    };
    /*
     * Defines an array that contains resource ids for the layout views
     * that get the Cursor column contents. The id is pre-defined in
     * the Android framework, so it is prefaced with "android.R.id"
     */
    private final static int[] TO_IDS = {
            android.R.id.text1
    };

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION =
            {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    Build.VERSION.SDK_INT
                            >= Build.VERSION_CODES.HONEYCOMB ?
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                            ContactsContract.Contacts.DISPLAY_NAME

            };

    // custom adapter
    // which allows us to have granular control
    // over each item in the list view

    private class MyCursorAdapter extends SimpleCursorAdapter {

        public MyCursorAdapter(android.content.Context context,
                               int layout,
                               android.database.Cursor c,
                               String[] from,
                               int[] to,
                               int flags) {
            super(context, layout, c, from, to, flags);
        }

        // when the view renders

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            CheckBox cBox = (CheckBox) v.findViewById(R.id.checkBox);
            TextView contactName = (TextView) v.findViewById(android.R.id.text1);
            // check the box if the current contact name is in the list of selected contacts
            if(cBox != null && contactName != null) {
                cBox.setChecked(selectedContactNames.contains(contactName.getText().toString()));
            }
            return v;
        }
    }

    private SimpleCursorAdapter cursorAdapter;

    // constructor & onCreate method required
    public ContactsFragment() {
        this.selectedContactNames = new ArrayList<>();
    }

    // Called just before the Fragment displays its UI
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Always call the super method first
        super.onCreate(savedInstanceState);
        LoaderManager.getInstance(this).initLoader(0, null, this);
    }

    // show the UI

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // load saved instance state if provided
        Bundle args = getArguments();
        if(args != null) {
            selectedContactNames = args.getStringArrayList("CONTACT_LIST");
        }
        return inflater.inflate(R.layout.contacts_fragment, container, false);
    }


    // show the list of contacts in the list view

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // get the view
        contactsList = (ListView) view.findViewById(android.R.id.list);
        // instantiate the adapter
        cursorAdapter = new MyCursorAdapter(
                getActivity(),
                R.layout.contact_list_item,
                null,
                FROM_COLUMNS, TO_IDS,
                0);
        // Sets the adapter for the ListView
        contactsList.setAdapter(cursorAdapter);
        // use this class as the on click listener
        contactsList.setOnItemClickListener(this);
    }

    // when the user click on a list view item (contact name)

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowID) {
        Cursor cursor = cursorAdapter.getCursor();
        // Move to the selected contact
        cursor.moveToPosition(position);
        int nameColIndex = cursor.getColumnIndex("display_name");
        // get their name
        String selectedContactName = cursor.getString(nameColIndex);
        // toggle contact selection depending on check box state
        CheckBox cBox = contactsList.getChildAt(position).findViewById(R.id.checkBox);
        if(cBox == null) return;
        if(!selectedContactNames.contains(selectedContactName) && !cBox.isChecked()) {
            selectedContactNames.add(selectedContactName);
            cBox.setChecked(!cBox.isChecked());
        } else if(selectedContactNames.contains(selectedContactName) && cBox.isChecked()) {
            selectedContactNames.remove(selectedContactName);
            cBox.setChecked(!cBox.isChecked());
        }
        // notify the other fragments of the change
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("CONTACT_LIST", selectedContactNames);
        getParentFragmentManager().setFragmentResult("CONTACT_LIST_UPDATE", bundle);
        // save the current state
        setArguments(bundle);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(
                getActivity(),
                ContactsContract.Contacts.CONTENT_URI,
                PROJECTION,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }
}
