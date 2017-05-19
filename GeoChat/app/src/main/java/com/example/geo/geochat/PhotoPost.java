package com.example.geo.geochat;


/**
 * Created by gustavo on 5/16/17.
 */

//TODO: as mentioned in MainActivity.java we could eventually add a date field to help sort posts in feed

public class PhotoPost {
    private String photoURL;
    private String text;


    public PhotoPost(){
        //stuff
    }

    public PhotoPost(String photoURL, String text){
        this.photoURL = photoURL;
        this.text = text;
    }

    public String getPhotoURL(){
        return this.photoURL;
    }

    public String getText(){
        return this.text;
    }

    public void setText(String text){
        this.text = text;
    }

    public void setPhotoURL(String photoURL){
        this.photoURL = photoURL;
    }

}
