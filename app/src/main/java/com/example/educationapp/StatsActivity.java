package com.example.educationapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class StatsActivity extends AppCompatActivity {

    private Cursor statsCursor;
    private EditText tweetView;
    private Button tweetButton;
    private String message;

    private String TWITTER_ACCESS_TOKEN;
    private String TWITTER_ACCESS_SECRET;
    private String TWITTER_CONSUMER_KEY;
    private String TWITTER_CONSUMER_SECRET;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        String totalFlashcardRows, totalFlashcardColumns, totalFlashcardTime;
        String attributes = "";

        SharedPreferences preferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);

        //----Assigns the flashcard-related TextViews
        TextView numberOfFlashcardsView = findViewById(R.id.numberOfFlashcardsView);
        TextView numberOfAttributesView = findViewById(R.id.numberOfAttributesView);
        TextView allFlashcardAttributes = findViewById(R.id.allFlashcardAttributes);
        TextView timeOnFlashcards = findViewById(R.id.timeOnFlashcards);

        //----Assigns the reminder-related TextViews
        TextView numberOfRemindersView = findViewById(R.id.numberOfRemindersView);
        TextView priorityTotal = findViewById(R.id.priorityTotal);
        TextView otherTotal = findViewById(R.id.otherTotal);
        TextView oldestReminder = findViewById(R.id.oldestReminder);
        TextView newestReminder = findViewById(R.id.newestReminder);
        TextView scoreView = findViewById(R.id.scoreView);
        TextView averageSuccessView = findViewById(R.id.averageSuccessView);

        //-------Opens database and retrieves the Flashcard table-----//
        SQLiteDatabase appDatabase = openOrCreateDatabase("app database", MODE_PRIVATE, null);
        appDatabase.execSQL("CREATE TABLE IF NOT EXISTS FLASHCARDS(Question VARCHAR,Answer VARCHAR);");
        statsCursor = appDatabase.rawQuery("SELECT * FROM FLASHCARDS ORDER BY RANDOM()", null);
        statsCursor.moveToFirst();

        totalFlashcardRows = toString(statsCursor.getCount());
        totalFlashcardColumns = toString(statsCursor.getColumnCount());
        totalFlashcardTime = String.valueOf(convertTimeToText(preferences.getLong("time",0)));

        numberOfFlashcardsView.setText(totalFlashcardRows);
        numberOfAttributesView.setText(totalFlashcardColumns);
        allFlashcardAttributes.setText(retrieveFlashcardAttributes(attributes));
        timeOnFlashcards.setText(totalFlashcardTime);

        //--------Opens the Reminders table----------//

        appDatabase.execSQL("CREATE TABLE IF NOT EXISTS REMINDERS(Title VARCHAR, Class_Name VARCHAR, Date VARCHAR, Priority VARCHAR);");
        statsCursor = appDatabase.rawQuery("SELECT * FROM REMINDERS", null);

        String totalNumberOfReminders = Integer.toString(statsCursor.getCount());
        numberOfRemindersView.setText(totalNumberOfReminders);

        statsCursor = appDatabase.rawQuery("SELECT * FROM REMINDERS WHERE Priority='true'", null);
        String totalNumberOfPriorityReminders = Integer.toString(statsCursor.getCount());  // Retrieves the total number of high-priority reminders
        priorityTotal.setText(totalNumberOfPriorityReminders);

        statsCursor = appDatabase.rawQuery("SELECT * FROM REMINDERS WHERE Priority='false'", null);
        String totalNumberOfOtherReminders = Integer.toString(statsCursor.getCount());  // Retrieves the total number of low-priority reminders
        otherTotal.setText(totalNumberOfOtherReminders);

        // Selects the entries from the Reminder table; sorted by date.
        statsCursor = appDatabase.rawQuery("SELECT *, substr(Date,7,4) AS year, substr(Date,4,2) AS month, substr(Date,1,2) AS day FROM REMINDERS ORDER BY year ASC,month ASC,day ASC", null);
        if (statsCursor.getCount() > 0) {
            statsCursor.moveToFirst();
            oldestReminder.setText(statsCursor.getString(0));
            statsCursor.moveToLast();
            newestReminder.setText(statsCursor.getString(0));
        }
        else {
            oldestReminder.setText("N/A");
            newestReminder.setText("N/A");
        }

        // Sets the stats for the CountDown Activity
        int countdownScore = preferences.getInt("score", 0);
        scoreView.setText(String.valueOf(countdownScore));

        int successRate = preferences.getInt("success_rate", 0);
        int totalCountdownScore = preferences.getInt("total_countdown", 0);

        if (totalCountdownScore != 0) {
            int average = (successRate * 100) / totalCountdownScore;
            averageSuccessView.setText(average + "%");
        }
        else {
            averageSuccessView.setText("N/A");
        }

        //--------Initializes Twitter related widgets and objects
        tweetView = findViewById(R.id.tweetView);
        tweetButton = findViewById(R.id.tweetButton);

        // Below are the keys and tokens for the Twitter feature in the app.
        TWITTER_CONSUMER_KEY = getResources().getString(R.string.consumer_key);
        TWITTER_CONSUMER_SECRET = getResources().getString(R.string.consumer_secret);
        TWITTER_ACCESS_TOKEN = getResources().getString(R.string.access_token);
        TWITTER_ACCESS_SECRET = getResources().getString(R.string.access_secret);

        String initialTweet;

        String name = preferences.getString("user_name", "");
        if (name.isEmpty()) {
            name = "User";
        }

        initialTweet = (name + " currently has " + totalFlashcardRows + " flashcards and "
                + totalNumberOfReminders + " reminders.\n" + name + " has studied the " +
                "flashcards for " + totalFlashcardTime + " so far.\n" + name + " has a CountDown " +
                "score of " + countdownScore + ".\nWish " + name + " luck with his/her studies.").trim();
        tweetView.setText(initialTweet);  // Sets the default tweet in the EditText widget.

        tweetButton.setOnClickListener(view -> {
            message = tweetView.getText().toString().trim();
            message += "\n#StudentHelper #StudyBuddy #UniStudier #StudyWithFlashcards";
            sendTweet();
        });  // Sends the tweet.

    }  // ends onCreate()


    public String toString(int number) {
        return Integer.toString(number);
    }  // ends toString()


    private String retrieveFlashcardAttributes(String attributes) {
        StringBuilder attributesBuilder = new StringBuilder(attributes);
        for (int i = 0; i < statsCursor.getColumnCount(); i++) {
            if (i == statsCursor.getColumnCount() - 1) {
                attributesBuilder.append(statsCursor.getColumnNames()[i]);
            }
            else {
                attributesBuilder.append(statsCursor.getColumnNames()[i]).append(", ");
            }
        }
        attributes = attributesBuilder.toString();
        return attributes;
    }  // ends retrieveFlashcardAttributes()


    private String convertTimeToText(long time) {
        String totalFlashcardTime = "";
        long seconds;
        long minutes;
        long hours;
        long days;

        time /= 1000;

        if (time > 0) {
            int count = 0;

            if (time >= 86400) {
                days = time / 86400;
                time %= 86400;
                if (days == 1) {
                    totalFlashcardTime += (days + " day ");
                }
                else {
                    totalFlashcardTime += (days + " days, ");
                }
                count++;
            }

            if (time >= 3600) {
                hours = time / 3600;
                time %= 3600;
                if (hours == 1) {
                    totalFlashcardTime += (hours + " hour, ");
                }
                else {
                    totalFlashcardTime += (hours + " hours, ");
                }
                count++;
            }

            if (time >= 60 && count < 2) {
                minutes = time / 60;
                time %= 60;
                if (minutes == 1) {
                    totalFlashcardTime += (minutes + " minute, ");
                }
                else {
                    totalFlashcardTime += (minutes + " minutes, ");
                }
                count++;
            }

            if (time >= 1 && count < 2) {
                seconds = time;
                if (seconds == 1) {
                    totalFlashcardTime += (seconds + " second");
                }
                else {
                    totalFlashcardTime += (seconds + " seconds");
                }
            }
        }


        else {
            totalFlashcardTime = "0 seconds";
        }

        return totalFlashcardTime;
    }  // ends convertTimeToText()


    private void sendTweet() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
        cb.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
        cb.setOAuthAccessToken(TWITTER_ACCESS_TOKEN);
        cb.setOAuthAccessTokenSecret(TWITTER_ACCESS_SECRET);

        Configuration configuration = cb.build();
        TwitterFactory factory = new TwitterFactory(configuration);
        Twitter twitter = factory.getInstance();
        try {
            int SDK_INT = android.os.Build.VERSION.SDK_INT;
            if (SDK_INT > 8) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                twitter.updateStatus(message);  // Sends tweet.
                Toast.makeText(this, "Tweet sent", Toast.LENGTH_SHORT).show();  // Alerts user for successful tweet
            }

        } catch (TwitterException e) {
            Toast.makeText(this, "Failed to send tweet", Toast.LENGTH_SHORT).show();  // Alerts user for failed tweet
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        tweetButton.setEnabled(true);
    }
}