package com.example.rap;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.rap.Fragments.ContactsFragment;
import com.example.rap.Fragments.AnswersFragment;
import com.example.rap.Fragments.SendFragment;

// simple pager adapter
// to map our fragment classes to the UI

public class MyPagerAdapter extends FragmentPagerAdapter {

    public MyPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new ContactsFragment();
            case 1:
                return new AnswersFragment();

            case 2:
                return new SendFragment();
            default:
                return null;

        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Contacts";
            case 1:
                return "Answers";
            case 2:
                return "Send";
            default:
                return "";

        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}




