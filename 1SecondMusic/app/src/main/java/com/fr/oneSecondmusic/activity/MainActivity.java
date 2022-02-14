package com.fr.oneSecondmusic.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fr.oneSecondmusic.R;
import com.fr.oneSecondmusic.adapter.PlaylistViewAdapter;
import com.fr.oneSecondmusic.connectors.ReqService;
import com.fr.oneSecondmusic.onbject.Playlist;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PlaylistViewAdapter.ItemClickListener {

    static RecyclerView recyclerView;
    static PlaylistViewAdapter adapter;
    public String playlistIdSelected = "";
    public String playlistNameSelected = "";
    public String playlistDescriptionSelected = "";
    public String deviceId;
    SharedPreferences sharedPreferences;
    FloatingActionButton fabAdd;
    FloatingActionButton fabPlay;
    FloatingActionButton fabEdit;
    private ReqService reqService;
    private ArrayList<Playlist> userPlaylist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        reqService = new ReqService(getApplicationContext());


        fabAdd = findViewById(R.id.fabAdd);
        fabPlay = findViewById(R.id.fabPlay);
        fabEdit = findViewById(R.id.fabEdit);

        initBtnListener();

        sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);


        recyclerView = findViewById(R.id.rvPlaylists);
        initUserPlaylist();

        reqService.getAvailableDevice(() -> {
            deviceId = reqService.getDeviceId();
            System.out.println("DEVICE ID : " + deviceId);
        });

    }

    private void initUserPlaylist() {
        // set up the RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        //add divider between row
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // list to populate the RecyclerView with
        ArrayList<Playlist> playlists = new ArrayList<>();

        adapter = new PlaylistViewAdapter(this, playlists);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        //request to get current user playlist en populate recycler view with it
        reqService.getUserPlaylists(() -> {
            userPlaylist = reqService.getPlaylists();
            //removing all the playlist objects in arrayList that are not 1sec playlists
            userPlaylist.removeIf(p -> !p.getName().contains("1Sec_"));

            int userPlaylistCount = 0;
            for (Playlist p : userPlaylist) {
                String playlistName = p.getName();
                if (playlistName.contains("1Sec_")) {
                    playlists.add(p);
                    adapter.notifyItemInserted(adapter.getItemCount() - 1);
                    userPlaylistCount++;
                }
            }
            if (userPlaylistCount == 0) {
                TextView tvNoPlaylist = findViewById(R.id.noPlaylist);
                tvNoPlaylist.setText(R.string.no_playlist);
            }
        });
    }

    private void initBtnListener() {
        fabAdd.setOnClickListener(view -> new AlertDialog.Builder(view.getContext())
                .setView(R.layout.activity_input_playlist_names)
                .setPositiveButton("Valider", (dialog, which) -> {
                    EditText etName = ((AlertDialog) dialog).findViewById(R.id.etInputPlaylistName);
                    EditText etDescription = ((AlertDialog) dialog).findViewById(R.id.etInputPlaylistDescription);

                    assert etDescription != null;
                    assert etName != null;
                    addPlaylist(etName.getText().toString(), etDescription.getText().toString());
                })
                .setNegativeButton("Annuler", (dialog, which) -> {
                })
                .show());
        fabPlay.setOnClickListener(view -> playPlaylist(playlistIdSelected));
        fabEdit.setOnClickListener(view -> editPlaylist());
    }

    private void editPlaylist() {
        if (!playlistIdSelected.isEmpty()) {
            Intent myIntent = new Intent(MainActivity.this, EditPlaylistActivity.class);
            myIntent.putExtra("playlistId", playlistIdSelected);
            myIntent.putExtra("playlistName", playlistNameSelected);
            myIntent.putExtra("playlistDescription", playlistDescriptionSelected);
            MainActivity.this.startActivity(myIntent);
        } else {
            Toast.makeText(this, "Select a playlist to edit", Toast.LENGTH_LONG).show();
        }
    }

    private void playPlaylist(String playlistIdSelected) {
        boolean isPlayerPlaying = reqService.getIsPlayerPlaying();
        if (isPlayerPlaying) {
            reqService.putPausePlayback();
        } else {
            reqService.putPlayPlaylist(playlistIdSelected);
        }


    }

    private void addPlaylist(String playlistName, String playlistDescription) {
        reqService.createNewPlaylist(sharedPreferences.getString("userid", ""), "1Sec_" + playlistName, playlistDescription, () -> {
            System.out.println("Playlist : " + reqService.getCreatedPlaylist().getName() + " created");
            playlistIdSelected = reqService.getCreatedPlaylist().getId();
            playlistNameSelected = reqService.getCreatedPlaylist().getName();
            playlistDescriptionSelected = reqService.getCreatedPlaylist().getId();
            editPlaylist();
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        //select playlist
        playlistIdSelected = userPlaylist.get(position).getId();
        playlistNameSelected = userPlaylist.get(position).getName();

        //change color of clicked tv item
        String slectedColor = "#1DB954";
        String defaultColorBlack = "#000000";
        String defaultColorWhite = "#FFFFFF";
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    recyclerView.getChildAt(i).setBackgroundColor(Color.parseColor(defaultColorBlack));
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    recyclerView.getChildAt(i).setBackgroundColor(Color.parseColor(defaultColorWhite));
                    break;
            }

        }
        recyclerView.getChildAt(position).setBackgroundColor(Color.parseColor(slectedColor));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.info) {
            new AlertDialog.Builder(MainActivity.this)
                    .setView(R.layout.activity_info)
                    .setPositiveButton("Ok", (dialog, which) -> {
                    })
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