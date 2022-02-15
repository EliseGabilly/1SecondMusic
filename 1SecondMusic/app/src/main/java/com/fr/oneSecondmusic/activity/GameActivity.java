package com.fr.oneSecondmusic.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fr.oneSecondmusic.MyApplication;
import com.fr.oneSecondmusic.R;
import com.fr.oneSecondmusic.connectors.ReqService;
import com.fr.oneSecondmusic.object.Album;
import com.fr.oneSecondmusic.object.Game;
import com.fr.oneSecondmusic.object.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameActivity extends AppCompatActivity {

    private ReqService reqService;
    private ArrayList<Album> albumList = new ArrayList<>();
    private Game game;

    Button buttonStart;
    Button button;
    TextView tvNbEntry;
    Spinner spinnerAlbum;
    Spinner spinnerTitle;

    private Song selectedSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        game = (Game) intent.getSerializableExtra("game");
        System.out.println(game.getPlaylistId()+" "+game.getPlaylistName());

        reqService = new ReqService(getApplicationContext());

        TextView playlistNameTv = findViewById(R.id.playlistName);
        playlistNameTv.setText(game.getPlaylistName());

        buttonStart = findViewById(R.id.buttonStart);

        if (game.isGameStarted()){
            buttonStart.setVisibility(View.GONE);
            //TODO
        } else {
            buttonStart.setOnClickListener(view -> buttonStart());
        }

    }

    private void buttonStart(){
        game.startGame();
        buttonStart.setVisibility(View.GONE);

        System.out.println("------------------------------"+game.getSongToGuess().size());

        button = findViewById(R.id.button);
        if(game.getSongToGuess().isEmpty()){
            TextView tvNoMusic = findViewById(R.id.noMusic);
            tvNoMusic.setText(R.string.no_music_game);
            button.setOnClickListener(view -> buttonNoMusic());
            button.setText(R.string.home);
        } else {
            button.setOnClickListener(view -> buttonGuess());
            tvNbEntry = findViewById(R.id.entryNb);
            tvNbEntry.setText("Music "+game.getCurrentSongNb()+" / "+game.getSongNb());
            spinnerAlbum = findViewById(R.id.spinnerAlbum);
            spinnerTitle = findViewById(R.id.spinnerTitle);
            spinnerAlbumInit();
        }
    }

    private void buttonNoMusic(){
        Intent myIntent = new Intent(GameActivity.this, MainActivity.class);
        GameActivity.this.startActivity(myIntent);
    }


    private void buttonGuess(){
        if(selectedSong !=null) {
            game.addGuess(selectedSong);
        } else {
            Toast.makeText(this, "Select a song to guess", Toast.LENGTH_LONG).show();
        }
    }


    private void spinnerAlbumInit(){

        List<String> lAlbum = albumList.stream().map(Album::getName).collect(Collectors.toList());
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lAlbum);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerAlbum.setAdapter(spinnerArrayAdapter);
        spinnerAlbum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSong = null;
                Album album = albumList.get(position);
                spinnerTitleInit(album);
                Toast.makeText(MyApplication.getContext(), "You selected album : "+album.getName(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
    }
    private void spinnerTitleInit(Album album){
        List<String> lTracks = album.getSelectedTracks().stream().map(Song::getName).collect(Collectors.toList());
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lTracks);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerTitle.setAdapter(spinnerArrayAdapter);
        spinnerTitle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSong = album.getSelectedTracks().get(position);
                Toast.makeText(MyApplication.getContext(), "You selected track : "+ selectedSong.getName(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.info) {
            new AlertDialog.Builder(GameActivity.this)
                    .setView(R.layout.activity_info)
                    .setPositiveButton("Ok", (dialog, which) -> { })
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // create an action bar buttons based on res/menu/menu
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}