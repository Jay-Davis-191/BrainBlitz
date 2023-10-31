package com.example.educationapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class FlashcardActivity extends AppCompatActivity {

    //----------Initializes the activity's widgets.
    private TextView flashcardContent;

    //----------Initializes the Database related objects.
    private Cursor flashcardCursor;
    private static SQLiteDatabase appDatabase;
    private int rowCount;

    //----------Initializes the phone-shake related objects and sensors.
    private SensorManager sensorManager;
    private ShakeDetector shakeDetector;
    private Sensor accelerometer;

    //----------Initializes the time-related variables.
    private SharedPreferences preferences;
    private long startTime;
    private long endTime;

    //----------Initializes the Tabs and the swipe gesture.
    private GestureDetectorCompat swipeGesture;
    private TabLayout flashcardTabs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        swipeGesture = new GestureDetectorCompat(this, new Swipe());

        // Assigns each widget an object reference.
        flashcardContent = findViewById(R.id.flashcardContent);
        Button newFlashcardButton = findViewById(R.id.newFlashcardButton);
        Button prevButton = findViewById(R.id.prevButton);
        Button nextButton = findViewById(R.id.nextButton);
        flashcardTabs = findViewById(R.id.flashcardTabs);

        preferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);  // Retrieves the value for the current total time spent on the Flashcard activity.
        startTime = System.currentTimeMillis();  // Calculates the start time for the activity. Used to calculate total time spent on Flashcards.

        //----------Assigns the phone-shake related objects.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeDetector = new ShakeDetector(() -> {  // Reveals answer when the phone shakes.
            endTime = System.currentTimeMillis();
            if (rowCount > 0 && (endTime - startTime) > 1000) {  // Included startTime to prevent the answer from being shown when the activity resumes. 1/2 second
                flashcardContent.setText(flashcardCursor.getString(1));
            }
        });

        // Opens the AddFlashcard activity which appears as a pop-up.
        newFlashcardButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddFlashcard.class);
            startActivity(intent);
        });

        // Goes to the previous row entry.
        prevButton.setOnClickListener(view -> {
            if (flashcardCursor.getCount() == 0) {
                Toast.makeText(this, "Please create more flashcards", Toast.LENGTH_SHORT).show();
            } else {
                if (!flashcardCursor.moveToPrevious()) {
                    flashcardCursor.moveToLast();
                }
                flashcardContent.setText(flashcardCursor.getString(0));  // Sets text to the data row's question.
            }
        });

        // Goes to the next row entry.
        nextButton.setOnClickListener(view -> {
            if (flashcardCursor.getCount() < 2) {
                Toast.makeText(this, "Please create more flashcards", Toast.LENGTH_SHORT).show();
            } else {
                if (!flashcardCursor.moveToNext()) {
                    flashcardCursor.moveToFirst();
                }
                flashcardContent.setText(flashcardCursor.getString(0));  // Sets text to the data row's question.
            }
        });

        //----------Sets up the sensors for the tabs.
        flashcardTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    moveToReminders();
                }
                else if (tab.getPosition() == 2) {
                    moveToCountdown();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.swipeGesture.onTouchEvent(event);
        return super.onTouchEvent(event);
    }


    @Override // Occurs when the user selects an item from the menu.
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.stats) {
            Intent intent = new Intent(this, StatsActivity.class);
            startActivity(intent);  // Moves to the stats page.
            return true;
        }

        else if (item.getItemId() == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);  // Moves to the settings page.
            return true;
        }
        return super.onOptionsItemSelected(item);
    }  // ends onOptionsItemSelected()



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
        preferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        Objects.requireNonNull(flashcardTabs.getTabAt(0)).select();
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        appDatabase = openOrCreateDatabase("app database", MODE_PRIVATE, null);
        createDatabase();
    }  // ends onResume()


    @Override
    protected void onPause() {
        calculateTotalTime();
        sensorManager.unregisterListener(shakeDetector);
        appDatabase.close();
        super.onPause();
    }  // ends onPause()


    private void createDatabase() {
        appDatabase.execSQL("CREATE TABLE IF NOT EXISTS FLASHCARDS(Question VARCHAR,Answer VARCHAR);");
        flashcardCursor = appDatabase.rawQuery("SELECT * FROM FLASHCARDS ORDER BY RANDOM()", null);
        flashcardCursor.moveToFirst();
        rowCount = flashcardCursor.getCount();
        if (rowCount > 0) {  // Checks if the Flashcard table is empty.
            flashcardContent.setText(flashcardCursor.getString(0));
        }
        else {
            flashcardContent.setText(R.string.flashcard_content);
        }
    }  // ends createDatabase()


    private void calculateTotalTime() {
        long currentTotalTime = preferences.getLong("time", 0);

        endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        long overallTotalTime = currentTotalTime + totalTime;
        preferences.edit().remove("time").putLong("time", overallTotalTime).apply();  // Updates the total preferences spent on the Flashcard activity.
    }  // ends calculateTotalTime()


    private class Swipe extends GestureDetector.SimpleOnGestureListener {  // Used to detect left-to-right swipe.
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            if (event2.getX() < event1.getX()) {  // left to right swipe
                moveToReminders();
            }
            return true;
        }
    }  // ends Swipe class


    private void moveToReminders() {
        Objects.requireNonNull(flashcardTabs.getTabAt(1)).select();
        Intent intent = new Intent(FlashcardActivity.this, ReminderActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.swipe_right_in, R.anim.swipe_left_out);  // Allows a sliding motion when changing activities.
    }  // ends moveToReminders()


    private void moveToCountdown() {
        Objects.requireNonNull(flashcardTabs.getTabAt(2)).select();  // Selects the Flashcard tab.
        Intent intent = new Intent(FlashcardActivity.this, CountdownActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.swipe_right_in, R.anim.swipe_left_out);  // Allows a sliding motion when changing activities.
    }  // ends moveToCountdown()

}  // ends FlashcardActivity()