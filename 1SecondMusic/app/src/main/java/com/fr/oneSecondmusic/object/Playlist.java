package com.fr.oneSecondmusic.object;

import android.widget.ImageView;

import com.fr.oneSecondmusic.MyApplication;
import com.fr.oneSecondmusic.connectors.ReqService;
import com.fr.oneSecondmusic.Utils;


public class Playlist {
    private final String id;
    private final String name;
    private final String description;
    private String imgURL;

    private ImageView imageView;

    public Playlist(String id, String name, String description) {
        this.name = name;
        this.id = id;
        this.description = description;
        ReqService reqService = new ReqService(MyApplication.getContext());

        reqService.getPlaylistIgmCover(id, () -> {
            imgURL = reqService.getImgURL();
            updateImageView();
        });
    }

    public void setImageView(ImageView iv){
        imageView = iv;
        updateImageView();
    }
    public void updateImageView(){
        if(imageView!=null){
            Utils.setImgViewFromURL(imageView, imgURL);
        }
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
}
