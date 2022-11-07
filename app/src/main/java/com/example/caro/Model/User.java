package com.example.caro.Model;

public class User {
    private  String sex;
    private String name;
    private  String pathImage;

    public User(String sex, String name, String pathImage) {
        this.sex = sex;
        this.name = name;
        this.pathImage = pathImage;
    }

    public String getSex() {
        return sex;
    }

    public String getName() {
        return name;
    }

    public String getPathImage() {
        return pathImage;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPathImage(String pathImage) {
        this.pathImage = pathImage;
    }
}
