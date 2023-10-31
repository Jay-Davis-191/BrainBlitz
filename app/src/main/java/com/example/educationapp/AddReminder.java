package com.example.educationapp;

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.util.Calendar;

public class AddReminder extends AppCompatActivity {

    private EditText reminderTitleEntry, reminderClassEntry, reminderDateEntry;
    private String reminderTitle, reminderClass, reminderDate;
    private SQLiteDatabase appDatabase;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        //----------Initializes and assigns the reminder-related widgets.
        Button reminderSubmitButton;
        reminderSubmitButton = findViewById(R.id.reminderSubmitButton);
        reminderTitleEntry = findViewById(R.id.reminderTitleEntry);
        reminderClassEntry = findViewById(R.id.reminderClassEntry);
        reminderDateEntry = findViewById(R.id.reminderDateEntry);

        //----------Opens the app's database and opens the Reminder table.
        appDatabase = openOrCreateDatabase("app database",MODE_PRIVATE,null);
        appDatabase.execSQL("CREATE TABLE IF NOT EXISTS REMINDERS(Title VARCHAR, Class_Name VARCHAR, Date VARCHAR, Priority VARCHAR);");

        //----------Initializes the objects used for creating the size of this activity. This is used for making this activity appear like a pop-up, instead of a new page.
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;  // Retrieves the width of the screen in pixels
        int height = dm.heightPixels;  // Retrieves the width of the screen in pixels
        getWindow().setLayout((int)(width*0.8), (int)(height*0.6));

        //----------Runs if the submit button is clicked.
        reminderSubmitButton.setOnClickListener(view -> {
            //----------Retrieves the entries from the three EditText widgets
            reminderTitle = reminderTitleEntry.getText().toString();
            reminderClass = reminderClassEntry.getText().toString();
            reminderDate = reminderDateEntry.getText().toString();

            if(validateReminderEntries()) {  // Adds the input from the user into the Reminders database.
                reformatDateEntry();
                Reminder reminder = new Reminder(reminderTitle, reminderClass, reminderDate, "false");
                appDatabase.execSQL("INSERT INTO REMINDERS VALUES('" + reminder.getTitle() + "', '" + reminder.getClassName() + "', '" + reminder.getDate() + "', '" + reminder.getPriority() + "');");
                Toast.makeText(this, "Reminder added", Toast.LENGTH_SHORT).show();
                finish();  // Closes the activity.
            }  // ends validateReminderEntries 'if' statement.
        });
    }  // ends onCreate()


    public Boolean validateReminderEntries() {
        if (reminderTitle.isEmpty()) {
            Toast.makeText(this, "No title given", Toast.LENGTH_SHORT).show();
            return false;
        }

        else if (reminderClass.isEmpty()) {
            Toast.makeText(this, "No class name given", Toast.LENGTH_SHORT).show();
            return false;
        }

        else if (reminderDate.isEmpty()) {
            Toast.makeText(this, "No due date given", Toast.LENGTH_SHORT).show();
            return false;
        }

        int length = reminderDate.length() - reminderDate.replace("/","").length();
        if (length == 0) {
            Toast.makeText(this, "Date must use '/'", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }  // ends validateEntries()


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void reformatDateEntry() {
        //----------Removes all possible leading and trailing spaces from all three entries.
        reminderTitle = reminderTitle.trim();
        reminderClass = reminderClass.trim();
        reminderDate = reminderDate.trim();

        if (reminderDate.contains("-")) {  // Reformats the date entry to user "/", to prevent an error in the database.
            reminderDate = reminderDate.replace("-", "/");
        }

        if (reminderDate.length() < 10) {  // Ensure the length is equal to 10,
            // eg. dd/mm/yyyy = total of 10 characters including the slashes.
            int count;

            count = reminderDate.length() - reminderDate.replace("/","").length();
            if (count == 1) {  // Prevents an error if the user only inputs the date and month, eg. 22/12.
                String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

                LocalDate currentDate = LocalDate.now();
                String dateEntry = reminderDate + "/" + year;

                String[] dueDateContent = dateEntry.split("/");  // Splits date into three parts.
                LocalDate dueDate = LocalDate.of(Integer.parseInt(dueDateContent[2]), Integer.parseInt(dueDateContent[1]), Integer.parseInt(dueDateContent[0]));  // Formats dueDate into the same format as currentDate.

                if (currentDate.isAfter(dueDate)) {
                    reminderDate += "/" + (Integer.parseInt(year) + 1);  // Adds the following year to the date entry. Eg. 22/12 -> 22/12/2023.
                }

                else {
                    reminderDate += "/" + year;  // Adds the current year to the date entry. Eg. 22/12 -> 22/12/2022.
                }
            }

            //----------Splits the date by "/" so all three parts are the correct length. This is to prevent sorting issues in the app.
            String[] dateEntrySplit = reminderDate.split("/");
            if (dateEntrySplit[0].length() == 1) {
                dateEntrySplit[0] = "0" + dateEntrySplit[0];  // Eg. 1/02/2022 -> 01/02/2022.
            }
            
            if (dateEntrySplit[1].length() == 1) {
                dateEntrySplit[1] = "0" + dateEntrySplit[1];  // Eg. 01/2/2022 -> 01/02/2022.
            }
            
            if (dateEntrySplit[2].length() == 2) {
                dateEntrySplit[2] = "20" + dateEntrySplit[2];  // Eg. 01/02/22 -> 01/02/2022.
            }
            reminderDate = dateEntrySplit[0] + "/" + dateEntrySplit[1] + "/" + dateEntrySplit[2];  // Places the three parts back together to make the updated date entry.
        }
    }  // ends formatDateEntry()
}  // ends AddReminder