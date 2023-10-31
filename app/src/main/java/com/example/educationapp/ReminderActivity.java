package com.example.educationapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Objects;

public class ReminderActivity extends AppCompatActivity {

    private LinearLayout listViewLayout;
    private ListView highPriorityWork, otherWorkList;
    private TextView highPriorityWorkView, otherWorkView;
    private static SQLiteDatabase appDatabase;
    private Cursor reminderCursor;
    private ArrayList<Reminder> highPriorityList;
    private ArrayList<Reminder> lowPriorityList;
    private Reminder reminder;
    private String reminderTitle, reminderClass, reminderDate;
    private GestureDetectorCompat swipeGesture;
    private TabLayout mainTabs;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        Button newReminderButton;
        swipeGesture = new GestureDetectorCompat(this, new Swipe());

        listViewLayout = findViewById(R.id.layout);
        mainTabs = findViewById(R.id.mainTabs);
        highPriorityWorkView = findViewById(R.id.highPriorityWorkView);
        otherWorkView = findViewById(R.id.otherWorkView);
        highPriorityWork = findViewById(R.id.highPriorityWorkList);
        otherWorkList = findViewById(R.id.otherWorkList);
        newReminderButton = findViewById(R.id.newReminderButton);

        newReminderButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddReminder.class);
            startActivity(intent);  // Opens the pop-up activity, AddReminder
        });

        highPriorityWork.setOnItemClickListener(((adapterView, view, position, id) -> {
            retrieveEntryValues(highPriorityList, position);
            if (!reminder.getResponse().equals("overdue")) {
                appDatabase.execSQL("UPDATE REMINDERS SET Priority = 'false' WHERE Title='"+ reminderTitle +"' AND Class_Name='" + reminderClass + "' AND Date='" + reminderDate + "'");
                retrieveTable();
            }
        }));  // Changes the current priority of the clicked Item. false -> true, true -> false

        highPriorityWork.setOnItemLongClickListener(((adapterView, view, position, id) -> {
            retrieveEntryValues(highPriorityList, position);
            appDatabase.execSQL("DELETE FROM REMINDERS WHERE Title='"+ reminderTitle +"' AND Class_Name='" + reminderClass + "' AND Date='" + reminderDate + "'");
            retrieveTable();
            return false;
        }));  // Deletes the high priority reminder from the Reminders table.

        otherWorkList.setOnItemClickListener(((adapterView, view, position, id) -> {
            retrieveEntryValues(lowPriorityList, position);
            appDatabase.execSQL("UPDATE REMINDERS SET Priority = 'true' WHERE Title='"+ reminderTitle +"' AND Class_Name='" + reminderClass + "' AND Date='" + reminderDate + "'");
            retrieveTable();
        }));  // Changes the current priority of the clicked Item. false -> true, true -> false

        otherWorkList.setOnItemLongClickListener(((adapterView, view, position, id) -> {
            retrieveEntryValues(lowPriorityList, position);
            appDatabase.execSQL("DELETE FROM REMINDERS WHERE Title='"+ reminderTitle +"' AND Class_Name='" + reminderClass + "' AND Date='" + reminderDate + "'");
            retrieveTable();
            return false;
        }));  // Deletes the low priority reminder from the Reminders table.

        mainTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    moveToFlashcards();
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
    }  // ends onCreate()


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.swipeGesture.onTouchEvent(event);
        return super.onTouchEvent(event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }  // Initializes the layout for the main menu. Creates the Settings and the Stats icons.


    @Override // Occurs when the user selects an item from the menu and moves to the selected activity.
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.stats) {
            Intent intent = new Intent(this, StatsActivity.class);
            startActivity(intent);
            return true;
        }

        else if (item.getItemId() == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }  // ends onOptionsItemSelected()


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void retrieveTable() {
        appDatabase.execSQL("CREATE TABLE IF NOT EXISTS REMINDERS(Title VARCHAR, Class_Name VARCHAR, Date VARCHAR, Priority VARCHAR);");
        setAdapters();
    }  // ends retrieveTable()


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        appDatabase = openOrCreateDatabase("app database",MODE_PRIVATE,null);
        retrieveTable();
        Objects.requireNonNull(mainTabs.getTabAt(1)).select();  // Selects the Flashcard tab.
        reminderCursor = appDatabase.rawQuery("SELECT * FROM REMINDERS", null);
        reminderCursor.moveToFirst();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @SuppressLint("SetTextI18n")
    private void setListViewsHeight() {
        @SuppressLint("Recycle") Cursor priorityEntries = appDatabase.rawQuery("SELECT * FROM REMINDERS WHERE Priority='true'", null);
        @SuppressLint("Recycle") Cursor otherEntries = appDatabase.rawQuery("SELECT * FROM REMINDERS WHERE Priority='false'", null);

        listViewLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            DisplayMetrics screenSize = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(screenSize);
            int screenHeight = screenSize.heightPixels;

            ViewGroup.LayoutParams highPriorityList = highPriorityWork.getLayoutParams();
            ViewGroup.LayoutParams otherList = otherWorkList.getLayoutParams();

            // Statements below determine the height of the two listviews depending on the number of each listview.
            if (priorityEntries.getCount() == 0) {
                highPriorityWorkView.setText("");
                highPriorityList.height = 0;
                highPriorityWork.setLayoutParams(highPriorityList);
                highPriorityWork.requestLayout();
            }  // Removes the high-priority reminders' title if empty.

            else {
                highPriorityList.height = screenHeight / 2;
                highPriorityWork.setLayoutParams(highPriorityList);
                highPriorityWork.requestLayout();
                if (otherEntries.getCount() == 0) {
                    highPriorityWorkView.setText("Reminders");
                }
                else {
                    highPriorityWorkView.setText("High Priority");
                }
            }

            if (otherEntries.getCount() == 0) {
                otherWorkView.setText("");
                otherList.height = 0;
                otherWorkList.setLayoutParams(otherList);
                otherWorkList.requestLayout();
                highPriorityList.height = screenHeight;
                highPriorityWork.setLayoutParams(highPriorityList);
                highPriorityWork.requestLayout();
            }  // Removes the low-priority reminders' title if empty, and sets the height of the low-priorities' listview.

            else if (otherEntries.getCount() > 0 && priorityEntries.getCount() > 0){
                otherWorkView.setText("Other");
                otherList.height = screenHeight / 2;
                otherWorkList.setLayoutParams(otherList);
                otherWorkList.requestLayout();
            }  // If both listviews have 1 or more entries each, each listview takes up half of the screen.

            else {
                otherWorkView.setText("Reminders"); // Changes the name to Reminders so the High Priority title does not appear if there are no high-priority reminders.
                otherList.height = screenHeight;
                otherWorkList.setLayoutParams(otherList);
                otherWorkList.requestLayout();
            }  // Occurs if there are low-priority reminders and no high-priority reminders.
        });  // ends listViewLayout.getViewTreeObserver().addOnGlobalLayoutListener()
    }  // ends setListViewsHeight()


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setAdapters() {
        highPriorityList = new ArrayList<>();
        lowPriorityList = new ArrayList<>();

        retrieveReminders();

        ReminderViewAdapter priorityArrayAdapter = new ReminderViewAdapter(this,  highPriorityList);
        ReminderViewAdapter otherArrayAdapter = new ReminderViewAdapter(this, lowPriorityList);
        highPriorityWork.setAdapter(priorityArrayAdapter);
        otherWorkList.setAdapter(otherArrayAdapter);
        setListViewsHeight();
    }  // ends setAdapters()


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void retrieveReminders() {
        String title;
        String name;
        String date;
        String priority;

        // Retrieves the entries from the Reminder table in order by date.
        reminderCursor = appDatabase.rawQuery("SELECT *, substr(Date,7,4) AS year, substr(Date,4,2) AS month, substr(Date,1,2) AS day FROM REMINDERS ORDER BY year ASC,month ASC,day ASC", null);
        reminderCursor.moveToFirst();

        while (!reminderCursor.isAfterLast()) {
            title = reminderCursor.getString(0);
            name = reminderCursor.getString(1);
            date = reminderCursor.getString(2);
            priority = reminderCursor.getString(3);

            if (priority.equals("true")) {
                highPriorityList.add(new Reminder(title, name, date, priority));
            }

            else {
                reminder = new Reminder(title, name, date, priority);
                reminder.calculateRemainingTime();
                String overdueStatus = reminder.checkOverdueStatus();  // Changes the priority of the reminder to "true", if the date is before the current date.

                if (overdueStatus.equals("true")) {
                    appDatabase.execSQL("UPDATE REMINDERS SET Priority = 'true' WHERE Title='"+ title +"' AND Class_Name='" + name + "' AND Date='" + date + "'");
                }

                if (reminder.getPriority().equals("true")) {
                    highPriorityList.add(reminder);
                }
                else {
                    lowPriorityList.add(reminder);
                }
            }
            reminderCursor.moveToNext();
        }  // ends while loop.
    }  // ends retrieveReminders()

    private void retrieveEntryValues(ArrayList<Reminder> list, int i) {
        reminder = list.get(i);
        reminderTitle = reminder.getTitle();
        reminderClass = reminder.getClassName();
        reminderDate = reminder.getDate();
    }  // ends retrieveEntryValues


    private class Swipe extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            if (event2.getX() > event1.getX()) {  // right to left swipe
                moveToFlashcards();
            }
            else if (event2.getX() < event1.getX()) {  // left to right swipe
                moveToCountdown();
            }
            return true;
        }
    }  // ends Swipe class


    private void moveToFlashcards() {
        Objects.requireNonNull(mainTabs.getTabAt(0)).select();  // Selects the Flashcard tab.
        Intent intent = new Intent(ReminderActivity.this, FlashcardActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.swipe_left_in, R.anim.swipe_right_out);  // Runs the sliding animation.
    }  // ends moveToFlashcards()


    private void moveToCountdown() {
        Objects.requireNonNull(mainTabs.getTabAt(2)).select();  // Selects the Flashcard tab.
        Intent intent = new Intent(ReminderActivity.this, CountdownActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.swipe_right_in, R.anim.swipe_left_out);  // Runs the sliding animation.
    }  // ends moveToCountdown()

}  // ends ReminderActivity()