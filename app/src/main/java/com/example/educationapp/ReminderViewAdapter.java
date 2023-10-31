package com.example.educationapp;

// This code was taken by URL: https://www.geeksforgeeks.org/how-to-read-data-from-sqlite-database-in-android/

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class ReminderViewAdapter extends ArrayAdapter<Reminder> {

    public ReminderViewAdapter(@NonNull Context context, ArrayList<Reminder> arrayList) {
        super(context, 0, arrayList);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // convertView which is recyclable view
        View currentItemView = convertView;

        // of the recyclable view is null then inflate the custom layout for the same
        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.reminder_item, parent, false);
        }

        // get the position of the view from the ArrayAdapter
        Reminder currentNumberPosition = getItem(position);

        // then according to the position of the view assign the titleView TextView for the same
        TextView titleView = currentItemView.findViewById(R.id.titleText);
        titleView.setText(currentNumberPosition.getTitle());

        // then according to the position of the view assign the classView TextView for the same
        TextView classView = currentItemView.findViewById(R.id.classNameText);
        classView.setText(currentNumberPosition.getClassName());

        // then according to the position of the view assign the dateView TextView for the same
        TextView dateView = currentItemView.findViewById(R.id.dateText);
        dateView.setText(currentNumberPosition.getDate());

        // then according to the position of the view assign the remainingView TextView for the same
        TextView remainingView = currentItemView.findViewById(R.id.remainingTimeText);
        currentNumberPosition.calculateRemainingTime();
        remainingView.setText(currentNumberPosition.getResponse());

        if (remainingView.getText().equals("Overdue")) {
            int colourRed = 0xffff0000;  // Using red entry from colors.xml would not change the text color.
            titleView.setTextColor(colourRed);
            remainingView.setTextColor(colourRed);
        }

        // then return the recyclable view
        return currentItemView;
    }

}
