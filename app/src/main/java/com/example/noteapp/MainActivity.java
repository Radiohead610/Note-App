package com.example.noteapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.noteapp.Adapters.NotesListAdapter;
import com.example.noteapp.Database.RoomDB;
import com.example.noteapp.Models.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    RecyclerView recyclerView;
    NotesListAdapter notesListAdapter;
    List<Notes> notes = new ArrayList<>();
    RoomDB database;
    FloatingActionButton fab_add;
    SearchView searchView_home;
    Notes selectedNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_home);
        fab_add = findViewById(R.id.fab_add);
        searchView_home = findViewById(R.id.searchView_home);

        database = RoomDB.getInstance(this);
        notes = database.mainDAO().getAll();

        updateRecycler(notes);

        //Whenever the user clicks on the "add" icon, it will go to the NotesTakerActivity -- onClick action -- save the content and set format
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
                startActivityForResult(intent, 101);        //101 adding a note; 102: editing the note
            }
        });
        //Search the notes
        searchView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //match each alphabet that the user enters
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String newText){
        List<Notes> filteredList = new ArrayList<>();
        //Add the notes that matches the search content in either title or description
        for(Notes singleNote : notes){
            if(singleNote.getTitle().toLowerCase().contains(newText.toLowerCase())
            || singleNote.getNotes().toLowerCase().contains(newText.toLowerCase())){
                filteredList.add(singleNote);
            }
        }
        //pass the updated filtered list to the adapter class to replace the one with all notes
        notesListAdapter.filterList(filteredList);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //save if it is 101
        if(requestCode == 101){
            //check result code then
            if(resultCode == Activity.RESULT_OK){
                //create the new note
                Notes new_notes = (Notes) data.getSerializableExtra("note");
                if(new_notes != null){
                    database.mainDAO().insert(new_notes);
                    //clear the note list, and re-add everything
                    notes.clear();
                    notes.addAll(database.mainDAO().getAll());
                    //notify the adapter to update the notes
                    notesListAdapter.notifyDataSetChanged();
                }

            }
        }
        else if(requestCode == 102){
            if(resultCode == Activity.RESULT_OK){
                Notes new_notes = (Notes) data.getSerializableExtra("note");
                database.mainDAO().update(new_notes.getID(), new_notes.getTitle(), new_notes.getNotes());
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();

            }
        }
    }

    private void updateRecycler(List<Notes> notes) {
        recyclerView.setHasFixedSize(true);

        //set a layout manager: 2 columns and layout is vertical
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        notesListAdapter = new NotesListAdapter(MainActivity.this, notes, notesClickListener);
        recyclerView.setAdapter(notesListAdapter);
    }

    //create a notes click listener
    private final NotesClickListener notesClickListener = new NotesClickListener() {
        //click to edit notes, req code 102
        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
            intent.putExtra("old_note", notes);
            startActivityForResult(intent, 102);
        }
        //long click to pin or delete
        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            selectedNote = new Notes();
            selectedNote = notes;
            showPopup(cardView);

        }
    };
    //use the menu to choose delete or pin action
    private void showPopup(CardView cardView) {
        PopupMenu popupMenu = new PopupMenu(this, cardView);
        popupMenu.setOnMenuItemClickListener(this);         //need to implement this on click method
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        //if the user chooses to pin/unpin
        if (item.getItemId() == R.id.pin){
                //unpin it if it is already pinned, and pin it if it is unpinned
                if(selectedNote.isPinned()){
                    database.mainDAO().pin(selectedNote.getID(), false);
                    Toast.makeText(MainActivity.this, "Unpinned!", Toast.LENGTH_SHORT).show();
                }
                else{
                    database.mainDAO().pin(selectedNote.getID(), true);
                    Toast.makeText(MainActivity.this, "Pinned!", Toast.LENGTH_SHORT).show();
                }
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();
            return true;
        }

        //if the user choose to delete the note
        else if(item.getItemId() == R.id.delete){
            database.mainDAO().delete(selectedNote);
            //remove the note from our list as well
            notes.remove(selectedNote);
            notesListAdapter.notifyDataSetChanged();
            Toast.makeText(MainActivity.this, "Note deleted!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}