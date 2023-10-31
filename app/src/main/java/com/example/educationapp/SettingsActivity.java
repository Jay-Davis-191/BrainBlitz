package com.example.educationapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private static SQLiteDatabase appDatabase;
    private EditText usernameEntry;
    private SharedPreferences preferences;
    private int chosenDifficultyAsInt;
    private int chosenTimeAsInt;
    private RadioGroup timesRadioGroup, difficultyRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button clearRemindersButton;
        Button restartTimeButton;
        Button clearFlashcardsButton;
        Button restartCountdownScoreButton;

        clearFlashcardsButton = findViewById(R.id.clearFlashcardsButton);
        clearRemindersButton = findViewById(R.id.clearRemindersButton);
        restartTimeButton = findViewById(R.id.restartTimeButton);
        usernameEntry = findViewById(R.id.usernameEntry);
        timesRadioGroup = findViewById(R.id.timesRadioGroup);
        difficultyRadioGroup = findViewById(R.id.difficultyRadioGroup);
        restartCountdownScoreButton = findViewById(R.id.restartCountdownScoreButton);

        appDatabase = openOrCreateDatabase("app database",MODE_PRIVATE,null);
        appDatabase.execSQL("CREATE TABLE IF NOT EXISTS FLASHCARDS(Question VARCHAR,Answer VARCHAR);");
        appDatabase.execSQL("CREATE TABLE IF NOT EXISTS REMINDERS(Title VARCHAR, Class_Name VARCHAR, Date VARCHAR, Priority VARCHAR);");

        clearFlashcardsButton.setOnClickListener(view -> {
            appDatabase.execSQL("DELETE FROM FLASHCARDS");
            appDatabase.execSQL("CREATE TABLE IF NOT EXISTS FLASHCARDS(Question VARCHAR,Answer VARCHAR);");
            Toast.makeText(this,"Flashcards deleted", Toast.LENGTH_SHORT).show();
        });  // Clears all entries from the Flashcards table

        clearRemindersButton.setOnClickListener(view -> {
            appDatabase.execSQL("DELETE FROM REMINDERS");
            appDatabase.execSQL("CREATE TABLE IF NOT EXISTS REMINDERS(Title VARCHAR, Class_Name VARCHAR, Date DATE, Priority VARCHAR);");
            Toast.makeText(this,"Reminders deleted", Toast.LENGTH_SHORT).show();
        });  // Clears all entries from the Reminders table

        restartTimeButton.setOnClickListener(view -> {
            preferences.edit().remove("time").apply();
            Toast.makeText(this,"Flashcard time restarted", Toast.LENGTH_SHORT).show();
        });  // Restarts the time spent on the Flashcard activity

        restartCountdownScoreButton.setOnClickListener(view -> {
            preferences.edit().remove("success_rate").apply();
            preferences.edit().remove("score").apply();
            preferences.edit().remove("total_countdown").apply();
            Toast.makeText(this,"Countdown restarted", Toast.LENGTH_SHORT).show();
        });

        timesRadioGroup.setOnCheckedChangeListener((radioGroup, i) -> checkTimeButton());

        difficultyRadioGroup.setOnCheckedChangeListener((radioGroup, i) -> checkDifficultyButton());


    }  // ends onCreate()


    @Override
    protected void onResume() {
        super.onResume();
        preferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        usernameEntry.setText(preferences.getString("user_name", "").trim());  // Sets the username to the saved entry.

        int pickedTime = preferences.getInt("provided_time", 0);
        if (pickedTime == 30) {
            timesRadioGroup.check(R.id.thirtySecButton);
        }
        else if (pickedTime == 60) {
            timesRadioGroup.check(R.id.sixtySecButton);
        }
        else if (pickedTime == 90) {
            timesRadioGroup.check(R.id.ninetySecButton);
        }

        int checkDifficulty = preferences.getInt("difficulty", 0);
        if (checkDifficulty == 0) {
            difficultyRadioGroup.check(R.id.easyButton);
        }
        else if (checkDifficulty == 1) {
            difficultyRadioGroup.check(R.id.mediumButton);
        }
        else if (checkDifficulty == 2) {
            difficultyRadioGroup.check(R.id.hardButton);
        }
        else if (checkDifficulty == 3) {
            difficultyRadioGroup.check(R.id.extremeButton);
        }



    }


    @Override
    protected void onPause() {
        preferences.edit().remove("user_name").putString("user_name", usernameEntry.getText().toString()).apply();  // Updates the current total-time.
        checkDifficultyButton();
        checkTimeButton();
        preferences.edit().remove("difficulty").putInt("difficulty",chosenDifficultyAsInt).apply();
        preferences.edit().remove("provided_time").putInt("provided_time", chosenTimeAsInt).apply();
        appDatabase.close();
        super.onPause();
    }


    private void checkDifficultyButton() {
        int radioId = difficultyRadioGroup.getCheckedRadioButtonId();
        RadioButton difficultyRadioButton = findViewById(radioId);
        String chosenDifficulty = difficultyRadioButton.getText().toString();
        switch (chosenDifficulty) {
            case "Easy":
                chosenDifficultyAsInt = 0;
                break;
            case "Medium":
                chosenDifficultyAsInt = 1;
                break;
            case "Hard":
                chosenDifficultyAsInt = 2;
                break;
            case "Extreme":
                chosenDifficultyAsInt = 3;
                break;
        }
    }


    private void checkTimeButton() {
        int radioId = timesRadioGroup.getCheckedRadioButtonId();
        RadioButton timesRadioButton = findViewById(radioId);
        String chosenTime = timesRadioButton.getText().toString();
        switch (chosenTime) {
            case "30":
                chosenTimeAsInt = 30;
                break;
            case "60":
                chosenTimeAsInt = 60;
                break;
            case "90":
                chosenTimeAsInt = 90;
                break;
        }
    }


}