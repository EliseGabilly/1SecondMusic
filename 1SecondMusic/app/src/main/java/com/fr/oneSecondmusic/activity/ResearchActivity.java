package com.fr.oneSecondmusic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fr.oneSecondmusic.R;
import com.fr.oneSecondmusic.connectors.ReqService;
import com.fr.oneSecondmusic.onbject.Album;
import com.fr.oneSecondmusic.onbject.Song;

import java.util.ArrayList;

public class ResearchActivity extends AppCompatActivity {

    private ReqService reqService;
    private ArrayList<Album> albumList = new ArrayList<>();
    private String playlistId;
    private String playlistName;
    private String playlistDescription;

    EditText editTxt;

    ImageView imgProp1;
    TextView tvInfo1;
    ImageView imgProp2;
    TextView tvInfo2;
    ImageView imgProp3;
    TextView tvInfo3;
    TextView editTxtInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_research);

        Intent intent = getIntent();
        playlistId = intent.getStringExtra("playlistId");
        playlistName = intent.getStringExtra("playlistName");
        playlistDescription = intent.getStringExtra("playlistDescription");

//        ActionBar actionBar = getSupportActionBar();
//        assert actionBar != null;
//        actionBar.setDisplayHomeAsUpEnabled(true);

        reqService = new ReqService(getApplicationContext());

        editTxt = findViewById(R.id.editTxtSearch);

        imgProp1 = findViewById(R.id.imgProp1);
        tvInfo1 = findViewById(R.id.tvInfo1);
        imgProp2 = findViewById(R.id.imgProp2);
        tvInfo2 = findViewById(R.id.tvInfo2);
        imgProp3 = findViewById(R.id.imgProp3);
        tvInfo3 = findViewById(R.id.tvInfo3);
        editTxtInfo = findViewById(R.id.editTxtInfo);

        intiListeners();
    }

    private void intiListeners() {
        editTxt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void performSearch(){
        reqService.getSearch(String.valueOf(editTxt.getText()), ()->{
            albumList = reqService.getAlbums();
            resetSearch();
            if(albumList.size()==0){
                editTxtInfo.setText(R.string.search_no_match);
                return;
            }
            albumList.get(0).updateSearchInfos(tvInfo1, imgProp1);
            if(albumList.size()==1)
                return;
            albumList.get(1).updateSearchInfos(tvInfo2, imgProp2);
            if(albumList.size()==2)
                return;
            albumList.get(2).updateSearchInfos(tvInfo3, imgProp3);
        });
    }

    private void resetSearch(){
        editTxtInfo.setText(" ");
        tvInfo1.setText(" ");
        imgProp1.setImageDrawable(null);
        tvInfo2.setText(" ");
        imgProp2.setImageDrawable(null);
        tvInfo3.setText(" ");
        imgProp3.setImageDrawable(null);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.info) {
            new AlertDialog.Builder(ResearchActivity.this)
                    .setView(R.layout.activity_info)
                    .setPositiveButton("Ok", (dialog, which) -> { })
                    .show();
        } else {
            //if not info then it's the back button
            loadEditActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // create an action bar buttons based on res/menu/menu
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void addAlbum0ToPlaylist(View view) {
        if(albumList.size()>0){
            reqService.addTracksToPlaylist((ArrayList<Song>) albumList.get(0).getTracks(), playlistId, () -> {});
            loadEditActivity();
        }
    }

    public void addAlbum1ToPlaylist(View view) {
        if(albumList.size()>1) {
            reqService.addTracksToPlaylist((ArrayList<Song>) albumList.get(1).getTracks(), playlistId, () -> {});
            loadEditActivity();
        }
    }

    public void addAlbum2ToPlaylist(View view) {
        if(albumList.size()>2) {
            reqService.addTracksToPlaylist((ArrayList<Song>) albumList.get(2).getTracks(), playlistId, () -> {});
            loadEditActivity();
        }
    }

    private void loadEditActivity() {
        Intent myIntent = new Intent(ResearchActivity.this, EditPlaylistActivity.class);
        myIntent.putExtra("playlistId", playlistId);
        myIntent.putExtra("playlistName", playlistName);
        myIntent.putExtra("playlistDescription", playlistDescription);
        ResearchActivity.this.startActivity(myIntent);
    }
}