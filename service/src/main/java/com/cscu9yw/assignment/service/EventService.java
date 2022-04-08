package com.cscu9yw.assignment.service;

import com.cscu9yw.assignment.datamodels.Event;
import com.cscu9yw.assignment.datamodels.User;

import java.util.ArrayList;
import java.util.List;

public interface EventService {
    //User methods
    boolean authenticate(String uid);
    User getUser(String uid);
    List<Event> getUsersEvents(String userId);

    //Events methods
    int createEvent(Event event);
    boolean deleteEvent(int eventId);
    Event getEvent(int id);
    ArrayList<Event> getAllEvents();
    ArrayList<Event> getAllEventsSanitized();
    List<User> getEventsAttendees(int eventId);
    int getEventAttendeesCount(int eventID);
    void validateAttendees();
    boolean registerUser(Event event, User user);
    boolean unregisterUser(Event event, User user);
}
