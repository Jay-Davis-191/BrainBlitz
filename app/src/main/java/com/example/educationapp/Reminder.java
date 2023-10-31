package com.example.educationapp;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.Period;

public class Reminder {

    private final String reminderTitle;
    private final String reminderClass;
    private final String reminderDate;
    private String reminderPriority;
    private String timeLeft;
    private String overdueStatus;

    public Reminder(String title, String className, String date, String priority) {
        reminderTitle = title;
        reminderClass = className;
        reminderDate = date;
        reminderPriority = priority;
        overdueStatus = "false";
    }

    
    public String getTitle() {
        return reminderTitle;
    }


    public String getClassName() {
        return reminderClass;
    }


    public String getDate() {
        return reminderDate;
    }


    public String getPriority() {
        return reminderPriority;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void calculateRemainingTime() {
        LocalDate currentDate = LocalDate.now();
        String[] dueDateContent = reminderDate.split("/");  // Splits date into three parts.
        LocalDate dueDate = LocalDate.of(Integer.parseInt(dueDateContent[2]), Integer.parseInt(dueDateContent[1]), Integer.parseInt(dueDateContent[0]));  // Formats reminderDate entry into the same format as currentDate.
        Period period = Period.between(currentDate, dueDate);  // Calculates the difference between the due date and the current date in days, weeks, and years - if appropriate.

        int years = Math.abs(period.getYears());
        int months = Math.abs(period.getMonths());
        int days = Math.abs(period.getDays());
        int length = 0;  // Only allow 2 periods of time for the date including: days, months and years. E.g. 5D 1Y = 5 days and 1 year.
        timeLeft = "";

        if (currentDate.isAfter(dueDate)) {  // Calls if the current date is after the due date.
            timeLeft = "Overdue";
            overdueStatus = "true";  // Automatically assigns high priority status to reminder if current date is after the due date.
        }

        else {  // If the current date is before the due date.
            if (years > 0) {
                length++;
                timeLeft += years + "Y ";
            }

            if (months > 0) {
                length++;
                timeLeft += months + "M ";
            }

            if (length < 2) {
                if (days == 0) {
                    timeLeft += "Today ";
                } else if (days == 1) {
                    timeLeft += "Tomorrow ";
                } else {
                    length++;
                    timeLeft += days + "D ";
                }
            }

            if (length != 0) {
                timeLeft += "Remaining";
            }
        }  // Sets the remaining time for the reminder.
    }  // ends calculateRemainingTime()


    public String checkOverdueStatus() {
        if (overdueStatus.equals("true")) {
            reminderPriority = "true";
        }
        return reminderPriority;
    }  // ends checkOverdueStatus()


    public String getResponse() {
        return timeLeft;
    }  // ends getResponse()
    
}  // ends Reminder class