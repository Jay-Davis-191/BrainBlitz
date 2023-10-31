package com.example.educationapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class CountdownActivity extends AppCompatActivity {

    private TextView timeView, requiredNumView;
    private Button firstNumButton;
    private Button secondNumButton;
    private Button thirdNumButton;
    private Button fourthNumButton;
    private Button fifthNumButton;
    private Button sixthNumButton;
    private Button passButton;
    private Button firstChosenButton;
    private char chosenOperator;

    private Random random;
    private final int REQUIRED_LENGTH = 6;
    private final ArrayList<Integer> NUM_OPTIONS = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 25, 50, 75, 100));
    private int generatedTotal, prevTotal, numberOfAvailableOperators, firstChosenNumber, secondChosenNumber;
    private ArrayList<String> calculations;
    private ArrayList<Integer> pickedNumbersList;
    private String output;
    
    private SharedPreferences preferences;
    
    private GestureDetectorCompat swipeGesture;
    private TabLayout countdownTabs;
    private CountDownTimer countdownTimer;

    private int selectedDifficulty;
    private long currentTimeLeft, INITIAL_TIME_LEFT;
    private int currentTotalScore, additionSubtractionCount, multiplicationDivisionCount;
    private int successRate, totalCountdownScore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        Button addButton, subtractButton, multiplyButton, divideButton, restartButton;

        countdownTabs = findViewById(R.id.countdownTabs);
        timeView = findViewById(R.id.timeView);
        requiredNumView = findViewById(R.id.requiredNumView);

        firstNumButton = findViewById(R.id.firstNumButton);
        secondNumButton = findViewById(R.id.secondNumButton);
        thirdNumButton = findViewById(R.id.thirdNumButton);
        fourthNumButton = findViewById(R.id.fourthNumButton);
        fifthNumButton = findViewById(R.id.fifthNumButton);
        sixthNumButton = findViewById(R.id.sixthNumButton);

        addButton = findViewById(R.id.addButton);
        subtractButton = findViewById(R.id.subtractButton);
        multiplyButton = findViewById(R.id.multiplyButton);
        divideButton = findViewById(R.id.divideButton);

        // Retrieves the value of the button's text if selected
        firstNumButton.setOnClickListener(this::checkNumButtonClicked);
        secondNumButton.setOnClickListener(this::checkNumButtonClicked);
        thirdNumButton.setOnClickListener(this::checkNumButtonClicked);
        fourthNumButton.setOnClickListener(this::checkNumButtonClicked);
        fifthNumButton.setOnClickListener(this::checkNumButtonClicked);
        sixthNumButton.setOnClickListener(this::checkNumButtonClicked);

        // Retrieves the operator from the selected button
        addButton.setOnClickListener(this::operatorButtonClicked);
        subtractButton.setOnClickListener(this::operatorButtonClicked);
        multiplyButton.setOnClickListener(this::operatorButtonClicked);
        divideButton.setOnClickListener(this::operatorButtonClicked);

        restartButton = findViewById(R.id.restartButton);
        Button solutionButton = findViewById(R.id.solutionButton);
        passButton = findViewById(R.id.passButton);

        swipeGesture = new GestureDetectorCompat(this, new Swipe());

        numberOfAvailableOperators = 4;
        firstChosenNumber = 0;
        secondChosenNumber = 0;
        chosenOperator = '0';

        requiredNumView.setText(String.valueOf(generatedTotal));

        solutionButton.setOnClickListener(view -> moveToSolution());

        restartButton.setOnClickListener(view -> {
            firstChosenNumber = 0;
            secondChosenNumber= 0;
            chosenOperator = 0;
            showNumbers();
            re_enableNumButtons(true);
        });

        passButton.setOnClickListener(view -> restartActivity());

        //----------Sets up the sensors for the tabs.
        countdownTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    moveToFlashcards();
                }
                else if (tab.getPosition() == 1) {
                    moveToReminders();
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
    protected void onResume() {
        super.onResume();
        preferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);

        INITIAL_TIME_LEFT = preferences.getInt("provided_time", 0) * 1000L;
        if ((int) INITIAL_TIME_LEFT == 0) {
            INITIAL_TIME_LEFT = 30L * 1000L;  // Default value of 30 seconds.
        }

        selectedDifficulty = preferences.getInt("difficulty", 0);
        currentTotalScore = preferences.getInt("score", 0);
        successRate = preferences.getInt("success_rate", 0);
        totalCountdownScore = preferences.getInt("total_countdown", 0);

        Objects.requireNonNull(countdownTabs.getTabAt(2)).select();
        restartActivity();
    }


    @Override
    protected void onPause() {
        preferences.edit().remove("score").putInt("score", currentTotalScore).apply();  // Updates the current total-time.
        preferences.edit().remove("success_rate").putInt("success_rate", successRate).apply();
        preferences.edit().remove("total_countdown").putInt("total_countdown", totalCountdownScore).apply();
        cancelTimer();
        super.onPause();
    }


    private void getOutput() {  // Prints the calculations made to make the generated total.
        output = "";
        for (String entry : calculations) {
            output += entry + "\n";
        }
    }


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


    private ArrayList<Integer> generateRandomList(ArrayList<Integer> list) {
        ArrayList<Integer> selectedNumbers = new ArrayList<>();
        for (int i = 0; i < REQUIRED_LENGTH; i++) {
            random = new Random();
            int randomIndex = random.nextInt(list.size());
            selectedNumbers.add(list.get(randomIndex));  // Retrieves 6 numbers from the available number list, randomly.
        }
        return selectedNumbers;
    }


    @SuppressLint("SetTextI18n")
    private void restartCountdownNumbers() {
        currentTimeLeft = INITIAL_TIME_LEFT;
        startTimer();
        do {
            additionSubtractionCount = 0;
            multiplicationDivisionCount = 0;
            generateTotal(generateRandomList(NUM_OPTIONS));
        } while (pickedNumbersList.contains(generatedTotal));  // Generates a new total if the total is equal to one of the number Button values.
        requiredNumView.setText(String.valueOf(generatedTotal));
        re_enableNumButtons(true);

        if (passButton.getText().equals("NEXT")) {
            passButton.setText("Pass");
        }
    }


    private void generateTotal(ArrayList<Integer> numbers) {
        int randomIndex;
        boolean status;

        pickedNumbersList = new ArrayList<>();
        calculations = new ArrayList<>();

        do {
            random = new Random();
            randomIndex = random.nextInt(numbers.size());
            pickedNumbersList.add(numbers.get(randomIndex));
            numbers.remove(randomIndex);

            if (pickedNumbersList.size() > 1) {
                do {
                    status = updateTotal(pickedNumbersList.get(pickedNumbersList.size() - 1));  // Updates the total by running an operator between the previous number and 1 of the available numbers.
                } while (!status);  // Status will be false if there is a remainder after two numbers are divided.
            }
            else {
                prevTotal = pickedNumbersList.get(0);
            }
        } while (numbers.size() != 0);  // Repeats this until all 6 randomly-picked numbers are used.
        getOutput();
        showNumbers();  // Shows the 6 randomly-picked numbers on the 6 number Buttons.
    }


    private boolean updateTotal(int currentNum) {
        random = new Random();
        int operator;
        operator = random.nextInt(numberOfAvailableOperators);

        // multiplicationDivisionCount is used to ensure the calculations use multiply or divide the appropriate times.
        if (operator >= 2 && multiplicationDivisionCount >= selectedDifficulty + 2) {
            do {
                operator = random.nextInt(numberOfAvailableOperators);
            } while (operator >= 2);
        }

        // additionSubtractionCount is used to ensure the calculations is not all addition and subtraction, but also includes multiply and divide.
        else if (operator < 2 && additionSubtractionCount >= REQUIRED_LENGTH - 1 - selectedDifficulty) {
            do {
                operator = random.nextInt(numberOfAvailableOperators);
            } while (operator < 2);
        }

        switch (operator) {
            case 0:
                generatedTotal = prevTotal + currentNum;
                calculations.add(prevTotal + " + " + currentNum + " = " + generatedTotal);
                prevTotal = generatedTotal;
                additionSubtractionCount++;
                return true;
            case 1:
                if (prevTotal > currentNum) {
                    generatedTotal = prevTotal - currentNum;
                    calculations.add(prevTotal + " - " + currentNum + " = " + generatedTotal);
                }
                else {
                    generatedTotal = currentNum - prevTotal;
                    calculations.add(currentNum + " - " + prevTotal + " = " + generatedTotal);
                }
                prevTotal = generatedTotal;
                additionSubtractionCount++;
                return true;

            case 2:
                generatedTotal =  prevTotal * currentNum;
                calculations.add(prevTotal + " * " + currentNum + " = " + generatedTotal);
                prevTotal = generatedTotal;
                multiplicationDivisionCount++;
                return true;
            case 3:
                if (prevTotal != 0 && currentNum != 0) {
                    if (prevTotal % currentNum == 0 || currentNum % prevTotal == 0) {
                        if (prevTotal > currentNum && prevTotal % currentNum == 0) {
                            generatedTotal = prevTotal / currentNum;
                            calculations.add(prevTotal + " / " + currentNum + " = " + generatedTotal);
                        } else if (currentNum > prevTotal && currentNum % prevTotal == 0) {
                            generatedTotal = currentNum / prevTotal;
                            calculations.add(currentNum + " / " + prevTotal + " = " + generatedTotal);
                        }
                        prevTotal = generatedTotal;
                        multiplicationDivisionCount++;
                        return true;
                    }
                }
        }  // ends switch() statement
        return false;
    }  // ends updateTotal()


    private void showNumbers() {
        firstNumButton.setText(String.valueOf(pickedNumbersList.get(0)));
        secondNumButton.setText(String.valueOf(pickedNumbersList.get(1)));
        thirdNumButton.setText(String.valueOf(pickedNumbersList.get(2)));
        fourthNumButton.setText(String.valueOf(pickedNumbersList.get(3)));
        fifthNumButton.setText(String.valueOf(pickedNumbersList.get(4)));
        sixthNumButton.setText(String.valueOf(pickedNumbersList.get(5)));
    }


    private void re_enableNumButtons(boolean status){
        firstNumButton.setEnabled(status);
        secondNumButton.setEnabled(status);
        thirdNumButton.setEnabled(status);
        fourthNumButton.setEnabled(status);
        fifthNumButton.setEnabled(status);
        sixthNumButton.setEnabled(status);
    }


    private void startTimer(){
        countdownTimer = new CountDownTimer(currentTimeLeft, 1000) {
            @Override
            public void onTick(long l) {
                currentTimeLeft = l / 1000;
                timeView.setText(String.valueOf(currentTimeLeft));
            }
            @Override
            public void onFinish() {
                re_enableNumButtons(false);
                moveToSolution();
            }
        }.start();
    }


    private void cancelTimer() {
        countdownTimer.cancel();
        timeView.setText(String.valueOf(currentTimeLeft));
    }


    @SuppressLint("NonConstantResourceId")
    private void checkNumButtonClicked(View view) {
        switch(view.getId()) {
            case R.id.firstNumButton:
                assignAsFirstNumButton(firstNumButton);
                break;
            case R.id.secondNumButton:
                assignAsFirstNumButton(secondNumButton);
                break;
            case R.id.thirdNumButton:
                assignAsFirstNumButton(thirdNumButton);
                break;
            case R.id.fourthNumButton:
                assignAsFirstNumButton(fourthNumButton);
                break;
            case R.id.fifthNumButton:
                assignAsFirstNumButton(fifthNumButton);
                break;
            case R.id.sixthNumButton:
                assignAsFirstNumButton(sixthNumButton);
                break;
        }
    }


    @SuppressLint("SetTextI18n")
    private void assignAsFirstNumButton(Button button) {
        if (firstChosenNumber == 0) {
            firstChosenNumber = Integer.parseInt(button.getText().toString());
            firstChosenButton = button;
            chosenOperator = '0';
        }

        if (firstChosenNumber != 0 && secondChosenNumber == 0 && chosenOperator == '0') {  // Ensures the most-recent Button clicked before the operator is assigned as the firstChosenNumber.
            firstChosenNumber = Integer.parseInt(button.getText().toString());
            firstChosenButton = button;
            }

        else {
            if (firstChosenButton != button) {
                secondChosenNumber = Integer.parseInt(button.getText().toString());
                if (chosenOperator != '0') {
                    int sum;
                    sum = calculate(button);
                    if (sum == generatedTotal) {
                        int length = String.valueOf(generatedTotal).length();
                        if (length < 3) {
                            length = 1;
                        }
                        long currentScore = (long) length * currentTimeLeft * (long) (selectedDifficulty + 1);
                        currentTotalScore += currentScore;
                        requiredNumView.setText("CORRECT -> " + currentScore + " pts");
                        passButton.setText("NEXT");
                        successRate++;
                        cancelTimer();
                    }
                }
            }
        }
    }


    private int calculate(Button button) {
        int currentSum = 0;
        boolean status = true;
        switch (chosenOperator) {
            case '+':
                currentSum = firstChosenNumber + secondChosenNumber;
                break;

            case '-':
                if (firstChosenNumber > secondChosenNumber) {
                    currentSum = firstChosenNumber - secondChosenNumber;
                }
                else {
                    currentSum = secondChosenNumber - firstChosenNumber;
                }
                break;

            case '*':
                currentSum = firstChosenNumber * secondChosenNumber;
                break;

            case '/':
                if (firstChosenNumber % secondChosenNumber == 0 || secondChosenNumber % firstChosenNumber == 0) {
                    if (((firstChosenNumber > secondChosenNumber) || (firstChosenNumber == secondChosenNumber))  && firstChosenNumber % secondChosenNumber == 0) {
                        currentSum = firstChosenNumber / secondChosenNumber;
                    } else if(secondChosenNumber > firstChosenNumber && secondChosenNumber % firstChosenNumber == 0) {
                        currentSum = secondChosenNumber / firstChosenNumber;
                    }
                    else {
                        status = false;
                    }
                }
                else {  // Exits calculate, so the program can obtain a different operator until the calculate will produce an int-type total.
                    status = false;
                }
                break;
        }  // ends switch() statement

        if(status) {
            updateFirstChosenButton(button, currentSum);
        }
        return currentSum;
    }


    private void updateFirstChosenButton(Button button, int sum) {
        button.setText(String.valueOf(sum));
        firstChosenButton.setEnabled(false);
        firstChosenButton.setText("");
        firstChosenButton = null;
        firstChosenNumber = 0;
        secondChosenNumber = 0;
        chosenOperator = '0';
    }


    @SuppressLint("NonConstantResourceId")
    private void operatorButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.addButton:
                chosenOperator = '+';
                break;
            case R.id.subtractButton:
                chosenOperator = '-';
                break;
            case R.id.multiplyButton:
                chosenOperator = '*';
                break;
            case R.id.divideButton:
                chosenOperator = '/';
                break;
        }
    }


    private void restartActivity() {
        do {
            if (currentTimeLeft != 0) {
                cancelTimer();
            }
            restartCountdownNumbers();
        } while (generatedTotal == 0 || String.valueOf(generatedTotal).length() < selectedDifficulty + 1 || String.valueOf(generatedTotal).length() > selectedDifficulty + 2);
        totalCountdownScore++;
    }


    private class Swipe extends GestureDetector.SimpleOnGestureListener {  // Used to detect left-to-right swipe.
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            if (event2.getX() > event1.getX()) {  // left to right swipe
                moveToReminders();
            }
            return true;
        }
    }  // ends Swipe class


    private void moveToSolution() {
        preferences.edit().remove("solution").putString("solution", output).apply();
        Intent intent = new Intent(this, CountdownSolution.class);
        startActivity(intent);
    }


    private void moveToReminders() {
        Objects.requireNonNull(countdownTabs.getTabAt(1)).select();
        Intent intent = new Intent(CountdownActivity.this, ReminderActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.swipe_left_in, R.anim.swipe_right_out);  // Allows a sliding motion when changing activities.
    }  // ends moveToReminders()


    private void moveToFlashcards() {
        Objects.requireNonNull(countdownTabs.getTabAt(0)).select();
        Intent intent = new Intent(CountdownActivity.this, FlashcardActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.swipe_left_in, R.anim.swipe_right_out);  // Runs the sliding animation.
    }  // ends moveToFlashcards()

}