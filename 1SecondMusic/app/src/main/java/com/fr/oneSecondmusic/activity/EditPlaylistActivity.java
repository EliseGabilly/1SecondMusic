package com.fr.oneSecondmusic.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fr.oneSecondmusic.R;
import com.fr.oneSecondmusic.adapter.AlbumRecyclerViewAdapter;
import com.fr.oneSecondmusic.connectors.ReqService;
import com.fr.oneSecondmusic.object.Album;
import com.fr.oneSecondmusic.object.Song;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class EditPlaylistActivity extends AppCompatActivity implements AlbumRecyclerViewAdapter.ItemClickListener {

    private ReqService reqService;
    private ArrayList<Album> albumList = new ArrayList<>();
    private String playlistId;
    private String playlistName;
    private String playlistDescription;

    RecyclerView recyclerViewAlbum;
    AlbumRecyclerViewAdapter adapter;

    FloatingActionButton fabAdd;
    FloatingActionButton fabSave;
    FloatingActionButton fabDel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_playlist);

        Intent intent = getIntent();
        playlistId = intent.getStringExtra("playlistId");
        playlistName = intent.getStringExtra("playlistName");
        playlistDescription = intent.getStringExtra("playlistDescription");

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        reqService = new ReqService(getApplicationContext());

        TextView playlistNameTv = findViewById(R.id.playlistName);
        playlistNameTv.setText(playlistName);
        fabAdd = findViewById(R.id.fabAdd);
        fabSave = findViewById(R.id.fabSave);
        fabDel = findViewById(R.id.fabDel);

        initBtnListener();

        recyclerViewAlbum = findViewById(R.id.rvAlbums);
        initAlbumList();

    }

    private void initAlbumList(){
        // set up the RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewAlbum.setLayoutManager(linearLayoutManager);

        //add divider between row
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewAlbum.getContext(), linearLayoutManager.getOrientation());
        recyclerViewAlbum.addItemDecoration(dividerItemDecoration);

        // list to populate the RecyclerView with
        ArrayList<Album> albumsNames = new ArrayList<>();

        adapter = new AlbumRecyclerViewAdapter(this, albumsNames);
        adapter.setClickListener(this);
        recyclerViewAlbum.setAdapter(adapter);

        //request to get all album from selected playlist en populate recycler view with it
        reqService.getAlbumsFromPlaylist(playlistId, () -> {
            albumList = reqService.getAlbums();
            int albumsCount = 0;
            for (Album a : albumList) {
                albumsNames.add(a);
                adapter.notifyItemInserted(adapter.getItemCount()-1);
                albumsCount++;
            }
            if(albumsCount == 0){
                TextView tvNoPlaylist = findViewById(R.id.noMusic);
                tvNoPlaylist.setText(R.string.no_music);
            }
        });

    }

    private void initBtnListener() {
        fabAdd.setOnClickListener(view -> addAlbum());
        fabSave.setOnClickListener(view -> savePlaylist());
        fabDel.setOnClickListener(view -> unfollowPlaylist());
    }

    private void addAlbum() {
        Intent myIntent = new Intent(EditPlaylistActivity.this, ResearchActivity.class);
        myIntent.putExtra("playlistId", playlistId);
        myIntent.putExtra("playlistName", playlistName);
        myIntent.putExtra("playlistDescription", playlistDescription);
        EditPlaylistActivity.this.startActivity(myIntent);
    }

    private void unfollowPlaylist() {
        new AlertDialog.Builder(EditPlaylistActivity.this)
                .setView(R.layout.activity_edit_delete)
                .setPositiveButton("Ok", (dialog, which) -> {
                    reqService.deleteUnfollowPlaylist(playlistId);
                    Intent myIntent = new Intent(EditPlaylistActivity.this, MainActivity.class);
                    EditPlaylistActivity.this.startActivity(myIntent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> { })
                .show();
    }

    private void savePlaylist(){
        //delete the playlist and create another
        //as volley does not support delete request with body (used for track deletion from playlist)
        reqService.deleteUnfollowPlaylist(playlistId);
        SharedPreferences sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        reqService.createNewPlaylist(sharedPreferences.getString("userid", ""), playlistName, playlistDescription, () -> {
            playlistId = reqService.getCreatedPlaylist().getId();
            playlistName = reqService.getCreatedPlaylist().getName();
            playlistDescription = reqService.getCreatedPlaylist().getDescription();
            fillPlaylist();
        });

    }
    private  void fillPlaylist(){
        ArrayList<Song> songsSelected = new ArrayList<>();
        for (Album a : albumList){
            songsSelected.addAll(a.getSelectedTracks());
        }
        if(songsSelected.size() == 0){
            return;
        }
        reqService.addTracksToPlaylist(songsSelected, playlistId, () -> {
            Intent myIntent = new Intent(EditPlaylistActivity.this, MainActivity.class);
            EditPlaylistActivity.this.startActivity(myIntent);
        });
    }



    @Override
    public void onItemClick(View view, int position) {
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.info) {
            new AlertDialog.Builder(EditPlaylistActivity.this)
                    .setView(R.layout.activity_info)
                    .setPositiveButton("Ok", (dialog, which) -> { })
                    .show();
        } else {
            //if not info then it's the back button
            Intent myIntent = new Intent(EditPlaylistActivity.this, MainActivity.class);
            EditPlaylistActivity.this.startActivity(myIntent);
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