package com.example.educationapp;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void test_date() {
        Reminder reminder = new Reminder("TitleTest","ClassTest","20/08/2022","false");
        System.out.println("The priority for the current reminder is " + reminder.getPriority());
        reminder.calculateRemainingTime();
        System.out.println("The time left for the reminder is " + reminder.getResponse());
    }

    @Test
    public void test_overdue() {
        String priorityStatus = "false";
        Reminder reminder = new Reminder("TitleTest","ClassTest","20/08/2021","false");
        System.out.println("The priority for the current reminder is " + priorityStatus);
        reminder.calculateRemainingTime();
        reminder.checkOverdueStatus();
        System.out.println("The new priority for the current reminder is " + reminder.getPriority());
        System.out.println("Remaining time for the reminder: " + reminder.getResponse());
    }
}