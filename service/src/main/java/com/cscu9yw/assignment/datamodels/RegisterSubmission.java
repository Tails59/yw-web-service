package com.cscu9yw.assignment.datamodels;

import com.cscu9yw.assignment.service.EventService;
import com.cscu9yw.assignment.service.ServiceImplementation;

/**
 * Validates a userId and eventId given through a POST request
 */
public class RegisterSubmission {
    private User user;
    private Event event;

    public RegisterSubmission(String userId, String eventId){
        EventService service = new ServiceImplementation();

        int parsedEventId;
        try {
            parsedEventId = Integer.parseInt(eventId);
        }catch(NumberFormatException e){
            return;
        }
        Event event = service.getEvent(parsedEventId);
        User user = service.getUser(userId);

        if(event == null || user == null){
            return;
        }

        this.user = user;
        this.event = event;
    }

    public User user() {
        return user;
    }

    public Event event() {
        return event;
    }
}
