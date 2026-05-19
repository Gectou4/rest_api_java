package com.g4.api.controller;

import com.g4.api.model.User;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserController {

    @GET
    @Path("/{id}")
    public Response getUser(@PathParam("id") int id) {
        User user = new User(id);
        if (!user.isLoaded()) {
            return Response.status(Response.Status.NOT_FOUND).entity("No user found").build();
        }
        return Response.ok(user.toArray()).build();
    }

    @GET
    @Path("/{id}/task")
    public Response getUserTasks(@PathParam("id") int id) {
        User user = new User(id);
        if (!user.isLoaded()) {
            return Response.status(Response.Status.NOT_FOUND).entity("No user found").build();
        }
        return Response.ok(user.getTasks()).build();
    }
}
