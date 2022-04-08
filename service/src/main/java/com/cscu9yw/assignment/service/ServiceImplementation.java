package com.cscu9yw.assignment.service;

import com.cscu9yw.assignment.datamodels.Event;
import com.cscu9yw.assignment.datamodels.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ServiceImplementation implements EventService{
    private static final String USER_DETAILS_API = "https://pmaier.eu.pythonanywhere.com";
    private static HashMap<Integer, Event> events = new HashMap<>();

    //Statically generate some hardcoded Events
    static{
        events.put(0, new Event("Intelligence Through the Ages", "Wallace Monument", "1 April 2025", "9am", "45 minutes", 1234));
        events.put(1, new Event("Internet of Stuff", "University of Stirling", "1 June 2022", "2pm", "90 minutes", 100));
        events.put(2, new Event("UI Design", "Forth Valley College (Falkirk)", "25 July 2023", "12pm", "50 minutes", 250));
        events.put(3, new Event("Malware Safety", "Cottrell Building", "2 August 2055", "4pm", "1 minute", 75));
    }

    /**
     * Get a user from the User Details API
     * @param uid the unique id of the user
     * @return A User object if the uid corresponds to a user, null otherwise.
     */
    public User getUser(String uid){
        try {
            URL url = new URL(USER_DETAILS_API + "/user/" + uid);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();

            while ((inputLine = reader.readLine()) != null) {
                content.append(inputLine);
            }

            reader.close();
            connection.disconnect();

            JsonObject json1 = new JsonParser().parse(content.toString()).getAsJsonObject();
            User user = new Gson().fromJson(json1.get("user"), User.class);

            return user;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("No user exists with given id (" + uid + ")");
        }

        return null;
    }

    /**
     * Validates all users that are attending this event
     */
    public void validateAttendees(){
        for(Event event : events.values()){
            ArrayList<String> attendees = (ArrayList<String>) event.getAttendees().clone();
            for(String user : attendees){
                if(!(getUser(user) instanceof User))
                    event.removeAttendee(user);
            }
        }
    }

    /**
     * Get all attending users for an event
     * As a side effect, also validates all users going to an event before returning.
     * @param eventId
     * @return
     */
    public List<User> getEventsAttendees(int eventId) {
        Event event = events.get(eventId);

        if (event == null) {
            return null;
        }

        ArrayList<User> users = new ArrayList<>();
        for (String userId : event.getAttendees()) {
            User user = getUser(userId);

            if (user == null) {
                event.removeAttendee(userId);
            } else {
                users.add(user);
            }
        }

        return users;
    }

    /**
     * Get all currently registered Events
     * @return An ArrayList of all Events
     */
    @Override
    public ArrayList<Event> getAllEvents(){
        validateAttendees();
        return new ArrayList(events.values());
    }

    /**
     * Get all currently registered Events
     * @return An ArrayList of all Events
     */
    @Override
    public ArrayList<Event> getAllEventsSanitized(){
        validateAttendees();
        ArrayList<Event> safeList = new ArrayList<>();

        for(Event e : events.values()){
            safeList.add(e.sanitise());
        }

        return safeList;
    }

    /**
     * Get the amount of attendees going to an event
     * @param eventID ID of an event to get attendee count for
     * @return An int representing the amount of people going to the event
     */
    @Override
    public int getEventAttendeesCount(int eventID) {
        return events.get(eventID).getAttendeeCount();
    }

    /**
     * Register a user as attending an event
     * @param eventParam Event to register the User for
     * @param user User that is going to the event
     */
    @Override
    public boolean registerUser(Event eventParam, User user) {
        Event event = events.get(eventParam.getEventId());
        if(event != null) {
            return event.addAttendee(user);
        }

        return false;
    }

    /**
     * Unregister a user from an event
     * @param eventParam Event to unregister the User from
     * @param user User that is no longer going to the Event
     */
    @Override
    public boolean unregisterUser(Event eventParam, User user) {
        Event event = events.get(eventParam.getEventId());

        if(event != null) {
            return event.removeAttendee(user);
        }

        return false;
    }

    /**
     * Store a new event in the system
     * @param event Event to register
     * @return The unique EventId of the new event
     */
    @Override
    public int createEvent(Event event) {
        Event newEvent = new Event(event.getDescription(), event.getLocation(), event.getDate(), event.getTime(), event.getDuration(), event.getMaxCapacity());
        events.put(newEvent.getEventId(), newEvent);
        return newEvent.getEventId();
    }

    /**
     * Get all Events a user is attending
     * @param userId Unique UserID to get events for
     * @return An ArrayList of Events the user is going to.
     */
    @Override
    public List<Event> getUsersEvents(String userId) {
        List<Event> attendingEvents = new ArrayList<>();

        for(Event event : events.values()){
            if(event.hasAttendee(userId)){
                attendingEvents.add(event);
            }
        }

        System.out.println(attendingEvents);

        return attendingEvents.size() > 0 ? attendingEvents : null;
    }

    @Override
    public boolean deleteEvent(int eventId) {
        return events.remove(eventId) != null;
    }

    /**
     * Checks whether a user with the given uid exists in the User Details API
     *
     * @param uid [String] uid to validate
     * @return True if a user with the given id exists
     */
    @Override
    public boolean authenticate(String uid){
        return getUser(uid) != null;
    }

    /**
     * Gets an event with the given ID, or null if no event exists.
     * @param id
     * @return
     */
    @Override
    public Event getEvent(int id) {
        return events.get(id);
    }

    public Event getSanitisedEvent(int id){
        return events.get(id).sanitise();
    }
}
