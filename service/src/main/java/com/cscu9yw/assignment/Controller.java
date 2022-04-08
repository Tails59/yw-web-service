package com.cscu9yw.assignment;

import com.cscu9yw.assignment.datamodels.Event;
import com.cscu9yw.assignment.datamodels.LoginFormSubmission;
import com.cscu9yw.assignment.datamodels.RegisterSubmission;
import com.cscu9yw.assignment.datamodels.User;
import com.cscu9yw.assignment.service.EventService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@RestController
@CrossOrigin
public class Controller {
    private final EventService service;

    public Controller(EventService eventService){
        service = eventService;
    }

    /**
     * UNUSED, was originally going to make a login screen but couldnt be bothered. too much diss work, too
     * many assignments, not enough time.
     *
     * Send a request to Login
     * @param user
     * @param response
     */
    @Deprecated
    @PostMapping("/login")
    public void login(@RequestBody LoginFormSubmission user, HttpServletResponse response){
        if(service.authenticate(user.userid())){
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("Access-Control-Allow-Origin", "*");

            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    /**
     * Request all stored events
     * @param userid [Optional] A userId, if supplied will only get events this user is registered to
     * @param response
     * @return A list of all events, or events a given user is registered to
     */
    @GetMapping("/events")
    public List<Event> getEvents(@RequestParam(required = false) String userid, HttpServletResponse response){
        response.setHeader("Access-Control-Allow-Origin", "*");
        System.out.println("GET request handled: response 200 (OK)");
        return userid != null ? service.getUsersEvents(userid) : service.getAllEventsSanitized();
    }


    /**
     * Get an event with a given ID
     * @param id
     * @param response
     * @return an Event with the associated ID
     * @return a Error 404 (Not Found) if no Event with the given ID is stored
     */
    @GetMapping("/events/{id}")
    public Event getEvent(@PathVariable int id, HttpServletResponse response){
        response.setHeader("Access-Control-Allow-Origin", "*");
        Event event = service.getEvent(id);

        if(event == null){
            System.out.println("No event with that id found");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        Event event2 = event.sanitise();

        response.setStatus(HttpServletResponse.SC_OK);
        System.out.println("GET request handled: response 200 (OK)");
        return event2;
    }

    /**
     * Register a user for an event
     * @param data [RegisterSubmission] Data passed in the POST body
     * @param response
     * @return Error 400 (Bad Request) if no data or invalid format was supplied
     * @return Error 409 (Conflict) if the user is already registered for the event
     * @return 200 (OK) if the user is registered successfully.
     */
    @PutMapping("/events/register")
    public void registerForEvent(@RequestBody RegisterSubmission data, HttpServletResponse response){
        if(data == null || data.event() == null || data.user() == null){
            System.out.println("Malformed User or Event data");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if(service.registerUser(data.event(), data.user())) {
            System.out.println("Registered user to event");
            System.out.println("PUT request handled: response 200 (OK)");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        System.out.println("User is already registered to that event");
        response.setStatus(HttpServletResponse.SC_CONFLICT);
    }

    /**
     * Unregister a user
     *
     * @param data
     * @param response
     * @return Error 400 (Bad Request) if the data supplied is invalid or null
     * @return Error 409 (Conflict) if the user is not registered to the event
     * @return 200 (OK) if the user is successfully unregistered
     */
    @PutMapping("/events/unregister")
    public void unregisterForEvent(@RequestBody RegisterSubmission data, HttpServletResponse response){
        if(data == null){
            System.out.println("Malformed User or Event data");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if(service.unregisterUser(data.event(), data.user())){
            response.setStatus(HttpServletResponse.SC_OK);
            System.out.println("Unregistered user " + data.user().getName() + " from  " + data.event().getDescription());
            System.out.println("PUT request handled: response 200 (OK)");
            return;
        }

        System.out.println("User is not registered for event");
        response.setStatus(HttpServletResponse.SC_CONFLICT);
    }

    private boolean authenticateAdminPassword(String input){
        String valid = "Basic " + Base64.getEncoder().encodeToString("admin:password1".getBytes(StandardCharsets.UTF_8));
        return valid.equals(input);
    }

    /**
     * Check if the user supplies an admin password
     * this protocol is quite possible the most insecure code i've ever written
     * @param base64password
     * @param response
     * @return
     */
    @GetMapping("admin/auth")
    public boolean authenticateAdmin(@RequestHeader("Authorization") String base64password, HttpServletResponse response){
        response.setHeader("Access-Control-Allow-Origin", "*");

        if(authenticateAdminPassword(base64password)){
            System.out.println("Authenticated administrator user");
            System.out.println("GET request handled: response 200 (OK)");
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        System.out.println("Admin password is incorrect");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    /**
     * Create a new event
     * Requires administrator password to be passed in Credentials header
     * @param event
     * @param response
     * @return Error 401 (Unauthorized) if no or invalid Credential header is supplied
     * @return Error 400 (Bad Request) if no or invalid Event data is supplied
     * @return 200 (OK) if the event is successfully created, with the new event path in the Location response header
     */
    @PostMapping("/admin/create-event")
    public void createEvent(@RequestBody Event event, @RequestHeader("Authorization") String base64password, HttpServletResponse response){
        if(!authenticateAdminPassword(base64password)){
            System.out.println("no admin auth provided");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if(event == null){
            System.out.println("Malformed event format");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        System.out.println("Created new event");
        int newEventId = service.createEvent(event);

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Location", "/events/" + newEventId);
        System.out.println("POST request handled: response 200 (OK)");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Get all attendees for an event, requires administrator password in Credentials header
     * @param eventid
     * @param response
     * @return Error 401 (Unauthorized) if invalid or no credentials supplied
     * @return Error 404 (Not Found) if no event could be found for the given ID
     * @return 200 (OK) if the attendees list is returned successfully
     */
    @GetMapping("/admin/get-attendees")
    public List<User> getAttendees(@RequestParam int eventid, @RequestHeader("Authorization") String base64password, HttpServletResponse response){
        System.out.println(base64password);
        if(!authenticateAdminPassword(base64password)){
            System.out.println("No admin auth provided");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        List<User> attendees = service.getEventsAttendees(eventid);
        if(attendees == null){
            System.out.println("No event with specified ID found");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        System.out.println("GET request handled: response 200 (OK)");
        response.setStatus(HttpServletResponse.SC_OK);
        return attendees;
    }

    /**
     * Deletes an event with a given EventId
     * @param eventid EventID to delete from the system
     * @param response
     *
     * @returns Error 400 (Bad Request) if the event ID is not given, or malformed.
     * @returns Error 404 (Not Found) if the ID is valid but no event exists associated with it
     * @returns 204 (No Content) if the event was successfully deleted from the system.
     */
    @DeleteMapping("/admin/delete-event")
    public void deleteEvent(@RequestParam int eventid, @RequestHeader("Authorization") String base64password, HttpServletResponse response){
        if(!authenticateAdminPassword(base64password)){
            System.out.println("No admin authentication provided");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if(service.deleteEvent(eventid)){
            System.out.println("Deleted event");
            System.out.println("DELETE request handled: response 204 (No Content)");
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }else{
            System.out.println("No event with specified ID");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
