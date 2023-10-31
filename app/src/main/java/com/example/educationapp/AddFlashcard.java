package com.example.educationapp;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddFlashcard extends AppCompatActivity {

    private EditText questionEntry, answerEntry;
    private String question, answer;
    private SQLiteDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flashcard);

        //----------Initializes and assigns the flashcard-related widgets.
        Button flashcardSubmitButton;
        questionEntry = findViewById(R.id.questionEntry);
        answerEntry = findViewById(R.id.answerEntry);
        flashcardSubmitButton = findViewById(R.id.flashcardSubmitButton);

        //----------Opens and assigns the app's database and opens the Flashcard table.
        appDatabase = openOrCreateDatabase("app database",MODE_PRIVATE,null);
        appDatabase.execSQL("CREATE TABLE IF NOT EXISTS FLASHCARDS(Question VARCHAR,Answer VARCHAR);");

        //----------Initializes the objects used for creating the size of this activity. This is used for making this activity appear like a pop-up, instead of a new page.
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;  // Retrieves the width of the screen in pixels
        int height = dm.heightPixels;  // Retrieves the height of the screen in pixels
        getWindow().setLayout((int)(width*0.8), (int)(height*0.6));  // Sets the screen size of the activity.

        //----------Runs if the submit button is clicked.
        flashcardSubmitButton.setOnClickListener(view -> {
            //----------Retrieves the entries from the two EditText widgets
           question = questionEntry.getText().toString();
           answer = answerEntry.getText().toString();

           if(validateFlashcardEntries()) {
               ContentValues flashcardValues = new ContentValues();
               flashcardValues.put("Question", question);
               flashcardValues.put("Answer", answer);
               appDatabase.insert("FLASHCARDS", null, flashcardValues);  // Adds the flashcard entry into the Flashcard table from the app's database.
               finish();  // Closes the activity.
           }  // ends validateFlashcardEntries 'if' statement.
        });
    }  // ends onCreate()


    private Boolean validateFlashcardEntries() {
        question = question.trim();  // Removes spaces before and after the String value.
        answer = answer.trim();  // Removes spaces before and after the String value.

        //----------Checks if either entries are empty. Alerts user if the entries are empty. This include spacing as well.
        if (question.isEmpty()) {
            Toast.makeText(this, "No question given", Toast.LENGTH_SHORT).show();
            return false;
        }

        else if (answer.isEmpty()) {
            Toast.makeText(this, "No answer given", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }  // ends validateEntries()

}  // ends AddFlashcard