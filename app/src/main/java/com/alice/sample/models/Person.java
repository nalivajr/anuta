package com.alice.sample.models;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class Person {

    private String name;
    private String lastName;
    private int photoId;

    public Person(String name, String lastName, int photoId) {
        this.name = name;
        this.lastName = lastName;
        this.photoId = photoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getPhotoId() {
        return photoId;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }
}
