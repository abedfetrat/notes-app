package com.abed.notepad;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewNoteActivity extends AppCompatActivity {

    private static final String TAG = "NewNoteActivity";
    private static final int SELECT_TAGS_REQUEST = 1;
    private static final int PICK_DATE_AND_TIME_REQUEST = 2;

    private EditText etTitle;
    private EditText etText;
    
    private DatabaseReference notesRef;
    
    private List<Tag> tags;
    private Reminder reminder;
    
    private boolean reminderChosen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        etTitle = findViewById(R.id.et_title);
        etText = findViewById(R.id.et_note);

        notesRef = FirebaseDatabase.getInstance().getReference().
                child(Constants.DB_KEY_USERS).
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                child(Constants.DB_KEY_NOTES);
        
        tags = new ArrayList<>();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemTag = menu.findItem(R.id.item_tag);
        MenuItem itemReminder = menu.findItem(R.id.item_reminder);

        if (tags.size() > 0) {
            itemTag.setTitle(getString(R.string.title_menu_item_edit_tag));
        } else {
            itemTag.setTitle(getString(R.string.title_menu_item_add_tag));
        }

        if (reminderChosen) {
            itemReminder.setTitle(getString(R.string.title_menu_item_edit_reminder));
        } else {
            itemReminder.setTitle(getString(R.string.title_menu_item_add_reminder));
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_new_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.item_tag:
                Intent intent = new Intent(this, TagActivity.class);
                intent.putExtra(Constants.KEY_TAGS, (ArrayList)tags);
                startActivityForResult(intent, SELECT_TAGS_REQUEST);
                return true;
            case R.id.item_reminder:
                Intent intent1 = new Intent(this, DateAndTimePickerActivity.class);
                if (reminderChosen) {
                    intent1.putExtra(Constants.KEY_TIME_IN_MILLIS, reminder.getTriggerTime());
                }
                startActivityForResult(intent1, PICK_DATE_AND_TIME_REQUEST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_TAGS_REQUEST) {
            if (resultCode == RESULT_OK) {
                tags = (ArrayList)data.getSerializableExtra(Constants.KEY_TAGS);
                invalidateOptionsMenu();
            }
        } else if (requestCode == PICK_DATE_AND_TIME_REQUEST) {
            if (resultCode == RESULT_OK) {
                String action = data.getAction();
                if (action.equals(DateAndTimePickerActivity.ACTION_ADD)) {
                    long triggerTime = data.getLongExtra(Constants.KEY_TIME_IN_MILLIS, 0);
                    if (reminderChosen) {
                        reminder.setTriggerTime(triggerTime);
                    } else {
                        int id = (int)(System.currentTimeMillis()/1000);
                        reminder = new Reminder(id, triggerTime);
                    }
                    reminderChosen = true;
                } else if (action.equals(DateAndTimePickerActivity.ACTION_DELETE)) {
                    reminder = null;
                    reminderChosen = false;
                }
                invalidateOptionsMenu();
            }
        }
    }

    private void setReminder(String title, String text, String tag, int id) {
        Intent intent = new Intent(this, AlarmService.class);
        intent.putExtra(Constants.KEY_TRIGGER_TIME, reminder.getTriggerTime());
        intent.putExtra(Constants.KEY_NOTIF_TITLE, title);
        intent.putExtra(Constants.KEY_NOTIF_TEXT, text);
        intent.putExtra(Constants.KEY_NOTIF_TAG, tag);
        intent.putExtra(Constants.KEY_NOTIF_ID, id);
        intent.setAction(AlarmService.ACTION_CREATE);
        startService(intent);
    }

    private void save() {
        String title = etTitle.getText().toString();
        String text = etText.getText().toString();
        // If note is not empty then save
        if (!title.isEmpty() || !text.isEmpty()) {
            String date = new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date());
            String id = notesRef.push().getKey();
            if (reminderChosen) {
                setReminder(title, text, id, reminder.getId());
                notesRef.child(id).setValue(new Note(id, title, text, date, tags, reminder));
            } else {
                notesRef.child(id).setValue(new Note(id, title, text, date, tags, null));
            }
        }
    }

    @Override
    public void finish() {
        save();
        super.finish();
    }
}