package com.fr.oneSecondmusic.object;

import java.util.List;

public class Game {

    private int songNb;
    private float duration;

    private List<Song> songToGuess;
    private List<Song> songGuessed;
    private int currentSongNb;

    public Game (){
        songNb = 10;
        duration = 2;
    }

    public Game(int songNb, float duration){
        this.songNb = songNb;
        this.duration = duration;
    }



}
