package com.cscu9yw.assignment.datamodels;

import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * An immutable class representing a User of the system
 * (this is also the same as an Attendee)
 */
public class User {
    @Id
    private final String uid;
    private final String name;
    private final List<String> interests;

    User(String id, String name, List<String> interests){
        uid = id;
        this.name = name;
        this.interests = interests;
    }

    public String getUid(){
        return this.uid;
    }

    public String getName(){
        return this.name;
    }

    public List<String> getInterests(){
        return this.interests;
    }

    public String toString(){
        return name + " (" + uid + ") ";
    }
}
