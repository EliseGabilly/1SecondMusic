package com.fr.oneSecondmusic.onbject;


import com.fr.oneSecondmusic.adapter.AlbumRecyclerViewAdapter;
import com.fr.oneSecondmusic.adapter.TrackRecyclerViewAdapter;

public class Song {
    private final Album album;
    private final String id;
    private final String name;
    private boolean isSelected;
    private TrackRecyclerViewAdapter.ViewHolder holder;

    public Song(Album album, String id, String name, boolean isSelected) {
        this.album = album;
        this.name = name;
        this.id = id;
        this.isSelected = isSelected;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setHolder(TrackRecyclerViewAdapter.ViewHolder holder){
        this.holder = holder;
    }

    public void changeSelected() {
        isSelected = !isSelected;
        updateSwitches();
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        updateSwitches();
    }

    private void updateSwitches(){
        if(holder!=null){
            holder.updateSwitch(isSelected);
        }
        AlbumRecyclerViewAdapter.ViewHolder albumHolder = album.getHolder();
        if(albumHolder!=null){
            albumHolder.updateUI(album);
        }
    }
}
