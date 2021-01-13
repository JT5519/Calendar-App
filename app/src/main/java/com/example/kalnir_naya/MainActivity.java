package com.example.kalnir_naya;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
//main activity, sets up the calendar grid view
public class MainActivity extends AppCompatActivity {
    CustomCalendarView customCalendarView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        customCalendarView = (CustomCalendarView)findViewById(R.id.custom_calendar_view);
    }
}
