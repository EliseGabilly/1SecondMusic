package com.fr.oneSecondmusic.object;

import com.fr.oneSecondmusic.MyApplication;
import com.fr.oneSecondmusic.connectors.ReqService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Game implements Serializable {

    private int songNb;
    private float duration;
    private String playlistId;

    private String playlistName;
    private Playlist playlist;

    private static List<Album> albumList;
    private static List<Song> songToGuess = new ArrayList<>();
    private static List<Song> songGuessed = new ArrayList<>();
    private int currentSongNb;

    private boolean isGameStarted =false;

    public Game (String playlistId){
        songNb = 10;
        duration = 2;
        this.playlistId = playlistId;
        intiSongList();
    }

    public Game(int songNb, float duration, String playlistId){
        this.songNb = songNb;
        this.duration = duration;
        this.playlistId = playlistId;
        intiSongList();
    }

    public void intiSongList(){
        ReqService reqService = new ReqService(MyApplication.getContext());
        reqService.getAlbumsFromPlaylist(playlistId, () -> {
            albumList = reqService.getAlbums();
        });

    }

    public void startGame(){
        List<Song> allSongs = new ArrayList<>();
        for(Album album : albumList) {
            allSongs.addAll(album.getSelectedTracks());
        }
        //TODO check if there is enough song for nbSong
        //TODO randomize selection
        songToGuess.add(allSongs.get(0));
        songToGuess.add(allSongs.get(1));

        isGameStarted = true;
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public void addGuess(Song song){
        songGuessed.add(song);
    }

    public int getSongNb() {
        return songNb;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public List<Song> getSongToGuess() {
        return songToGuess;
    }

    public List<Song> getSongGuessed() {
        return songGuessed;
    }

    public int getCurrentSongNb() {
        return currentSongNb;
    }
}
