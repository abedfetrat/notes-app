package com.abed.notepad;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TagActivity extends AppCompatActivity {

    private static final String TAG = "TagActivity";
    private DatabaseReference tagsRef;
    private List<Tag> tags;
    private TagsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText et = findViewById(R.id.et_toolbar);
        final ListView lv  = findViewById(R.id.lv_tags);
        final Button btn = findViewById(R.id.btn_create);

        Intent intent = getIntent();
        List<Tag> checkedTags = (ArrayList) intent.getSerializableExtra(Constants.KEY_TAGS);

        tagsRef = FirebaseDatabase.getInstance().getReference().
                child(Constants.DB_KEY_USERS).
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                child(Constants.DB_KEY_TAGS);
        tagsRef.addValueEventListener(valueEventListener);

        tags = new ArrayList<>();
        adapter = new TagsAdapter(this, tags, checkedTags);
        lv.setAdapter(adapter);

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String etText = et.getText().toString().toLowerCase().trim();
                if (!etText.isEmpty() && !tagsContain(etText)) {
                    String text = getString(R.string.tag_activity_btn_create) + " " + etText;
                    btn.setVisibility(View.VISIBLE);
                    btn.setText(text);
                } else {
                    btn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox cb = view.findViewById(R.id.cb);
                cb.setPressed(true);
                cb.setChecked(!cb.isChecked());
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = tagsRef.push().getKey();
                String text = et.getText().toString().trim();
                text = text.substring(0, 1).toUpperCase() + text.substring(1);
                Tag tag = new Tag(id, text);
                tags.add(tag);
                adapter.notifyDataSetChanged();
                tagsRef.child(id).setValue(tag);
                et.setText("");
                et.clearFocus();
            }
        });

    }

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            tags.clear();
            for (DataSnapshot tagSnapshot : dataSnapshot.getChildren()) {
                Tag tag = tagSnapshot.getValue(Tag.class);
                tags.add(tag);
            }
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean tagsContain(String s) {
        for (Tag tag : tags) {
            if (tag.getName().toLowerCase().equals(s))
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra(Constants.KEY_TAGS, (ArrayList)adapter.getCheckedTags());
        setResult(RESULT_OK, result);
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        tagsRef.removeEventListener(valueEventListener);
        super.onStop();
    }
}
