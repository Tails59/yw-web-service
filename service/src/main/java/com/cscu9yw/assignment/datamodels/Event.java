package com.cscu9yw.assignment.datamodels;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;

import java.util.*;

public class Event {
    private static int eventCounter = 0;

    @Id
    private final int eventId;
    private final String description;
    private final String location;
    private final String date;
    private final String time;
    private final String duration;
    private final int maxCapacity;
    private int attendeeCount;

    /**
     * A HashSet of all user IDs that are attending this event
     */
    private ArrayList<String> attendees;

    private Event(int eventId, String description, String location, String date, String time, String duration, int maxCapacity) {
        this.eventId = eventId;
        this.description = description;
        this.location = location;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.maxCapacity = maxCapacity;

        attendees = new ArrayList<>();
    }

    /**
     * Generates a new Event object with a unique ID
     * @param description
     * @param location
     * @param date
     * @param time
     * @param duration
     * @param maxCapacity
     */
    public Event(@JsonProperty("description") String description, @JsonProperty("location") String location,
                 @JsonProperty("date") String date, @JsonProperty("time") String time,
                 @JsonProperty("duration") String duration, @JsonProperty("maxCapacity") int maxCapacity){
        this(eventCounter++, description, location, date, time, duration, maxCapacity);
    }

    public Event(Event oldEvent) {
        this.eventId = oldEvent.eventId;
        this.description = oldEvent.description;
        this.location = oldEvent.location;
        this.date = oldEvent.date;
        this.time = oldEvent.time;
        this.duration = oldEvent.duration;
        this.maxCapacity = oldEvent.maxCapacity;
        this.attendeeCount = oldEvent.attendeeCount;
        System.out.println(oldEvent.attendeeCount);
        System.out.println(attendeeCount);
        attendees = new ArrayList<>();
    }

    /**
     * Check if a user (through their userid) is registered for this event
     * @param user
     * @return
     */
    public boolean hasAttendee(User user){
        return hasAttendee(user.getUid());
    }

    public boolean hasAttendee(String givenUserId){
        for(String storedUserID : attendees){
            if(storedUserID.equals(givenUserId)){
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a user to this events attendee list, as long as the capacity isnt reached
     * and the user isnt already registered to attend.
     * @param user
     * @return True if the user was successfully registered, false if the event is at capacity or if the user
     * already registered.
     */
    public boolean addAttendee(User user){
        if(attendees.size() >= maxCapacity){
            return false;
        }

        if(hasAttendee(user)) {
            System.out.println("User already registered for event!");
            return false;
        }

        attendees.add(user.getUid());
        System.out.println(user + " registered for " + this);
        attendeeCount = attendees.size();
        return true;
    }

    /**
     * Removes a user from the attending set, or does nothing if they are not attending
     * @param user
     */
    public boolean removeAttendee(User user) {
        return removeAttendee(user.getUid());
    }

    public boolean removeAttendee(String userId){
        attendees.indexOf(userId);
        int index = -1;

        for(int i = 0; i < attendees.size(); i++){
            if(attendees.get(i).equals(userId)){
                index = i;
                break;
            }
        }

        if(index != -1) {
            attendees.remove(index);
            System.out.println(userId + " unregistered for " + this);
            attendeeCount--;
            return true;
        }

        return false;
    }

    /**
     * Create a new event with the sensitive data removed (i.e. the attendees list)
     * @return
     */
    public Event sanitise(){
        this.attendeeCount = getAttendeeCount();
        return new Event(this);
    }


    public ArrayList<String> getAttendees(){
        return attendees;
    }

    public int getAttendeeCount(){
        return Math.max(attendees.size(), attendeeCount);
    }

    public int getEventId() {
        return eventId;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getDuration() {
        return duration;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public String toString(){
        return description + " (" + eventId + ") ";
    }
}
