package com.example.noteapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.noteapp.Models.Notes;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NotesTakerActivity extends AppCompatActivity {

    EditText editText_title, editText_notes;
    ImageView imageView_save;
    Notes notes;
    boolean isOldNote = false;      //by default, not old note

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_taker);

        //initialize the items
        imageView_save = findViewById(R.id.imageView_save);
        editText_title = findViewById(R.id.editText_title);
        editText_notes = findViewById(R.id.editText_notes);

        notes = new Notes();
        //if it finds the old note, it will update it, but if the old note is not found, it will crash, so better to use try-catch
        try{
            notes = (Notes) getIntent().getSerializableExtra("old_note");
//            assert notes != null;
            editText_title.setText(notes.getTitle());
            editText_notes.setText(notes.getNotes());
            isOldNote = true;       //becomes an old note now
        } catch (Exception e) {
            e.printStackTrace();
        }

        //activate the event
        imageView_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = editText_title.getText().toString();
                String description = editText_notes.getText().toString();

                //return a message if the note is empty, else just save the note
                if(description.isEmpty()){
                    Toast.makeText(NotesTakerActivity.this, "Please add some notes.", Toast.LENGTH_SHORT).show();
                }
                else {
                    //set a formatter to format the date
                    SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm a");     //weekday, date, month, year, hour, minute, AM or PM
                    Date date = new Date();

                    //check if isOldNote is false, if it is false, add a new note
                    if(!isOldNote){
                        //initialize notes
                        notes = new Notes();
                    }

                    notes.setTitle(title);
                    notes.setNotes(description);
                    notes.setDate(formatter.format(date));      //format the date first

                    //need to make the class serializable before passing it to the intent
                    Intent intent = new Intent();
                    intent.putExtra("note", notes);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });


    }
}