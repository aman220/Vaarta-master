package com.example.application.models;

import java.io.Serializable;

public class User implements Serializable {
    public String name;
    public String image;
    public String email;
    public String token;
    public String id;
    public String status;
    public String availablity;


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
