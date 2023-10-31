package com.example.educationapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CountdownSolution extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown_solution);

        SharedPreferences preferences;
        TextView solutionView;
        String output;

        //----------Initializes the objects used for creating the size of this activity. This is used for making this activity appear like a pop-up, instead of a new page.
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;  // Retrieves the width of the screen in pixels
        int height = dm.heightPixels;  // Retrieves the width of the screen in pixels
        getWindow().setLayout((int)(width*0.8), (int)(height*0.6));

        preferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        solutionView = findViewById(R.id.solutionView);

        output = preferences.getString("solution", "");

        solutionView.setText(output);
    }
}