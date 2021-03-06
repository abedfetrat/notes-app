package com.abed.notepad;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateAndTimePickerActivity extends AppCompatActivity {
    public static final String ACTION_ADD = "action_add";
    public static final String ACTION_DELETE = "action_delete";

    private Button btnDate;
    private Button btnTime;

    private Calendar calendar;

    private boolean dateIsSet;
    private boolean timeIsSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_and_time_picker);

        btnDate = findViewById(R.id.btn_date);
        btnTime = findViewById(R.id.btn_time);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnDelete = findViewById(R.id.btn_delete);

        calendar = Calendar.getInstance();

        Intent intent = getIntent();
        long triggerTime = intent.getLongExtra(Constants.KEY_TIME_IN_MILLIS, 0);
        if (triggerTime > 0) {
            calendar.setTimeInMillis(triggerTime);
            dateIsSet = true;
            timeIsSet = true;
            btnDate.setText(formatDate());
            btnTime.setText(formatTime());
            btnDelete.setVisibility(View.VISIBLE);
            setTitle(getString(R.string.title_activity_date_and_time_picker_2));
        }
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(DateAndTimePickerActivity.this, onDateSetListener, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                dpd.show();
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(DateAndTimePickerActivity.this, onTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(DateAndTimePickerActivity.this)).show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dateIsSet && timeIsSet) {
                    // Check if time has passed
                    if (!(calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())) {
                        Calendar now = Calendar.getInstance();
                        Intent result = new Intent();
                        result.putExtra(Constants.KEY_TIME_IN_MILLIS, calendar.getTimeInMillis());
                        result.setAction(ACTION_ADD);
                        setResult(RESULT_OK, result);
                        finish();
                    } else {
                        Toast.makeText(DateAndTimePickerActivity.this, getString(R.string.message_time_passed), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DateAndTimePickerActivity.this, getString(R.string.message_set_date_time), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                result.setAction(ACTION_DELETE);
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }

    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            btnDate.setText(formatDate());
            dateIsSet = true;
        }
    };

    private TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            btnTime.setText(formatTime());
            timeIsSet = true;
        }
    };

    private String formatDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
    }

    private String formatTime() {
        String pattern;
        if (DateFormat.is24HourFormat(DateAndTimePickerActivity.this)) {
            pattern = "HH:mm";
        } else {
            pattern = "hh:mm aaa";
        }

        return new SimpleDateFormat(pattern, Locale.US).format(calendar.getTime());
    }
}