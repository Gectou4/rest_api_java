package com.g4.api.controller;

import com.g4.api.model.Task;
import com.g4.api.model.TaskStatus;
import com.g4.api.model.User;
import com.g4.api.model.UserTask;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/task")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class TaskController {

    @GET
    public Response getAll() {
        Task task = new Task();
        return Response.ok(task.getAll()).build();
    }

    @POST
    public Response createTask(@FormParam("title") String title,
                               @FormParam("description") String description,
                               @FormParam("status") Integer status) {
        if (title == null || title.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Title is required")
                    .build();
        }

        Task task = new Task();
        task.setTitle(title.trim());
        task.setDescription(description != null ? description.trim() : "");
        task.setStatus(status != null ? TaskStatus.fromValue(status) : TaskStatus.BACKLOG);

        if (task.save()) {
            return Response.status(Response.Status.CREATED)
                    .entity(task.toArray())
                    .build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Unable to create new Task")
                .build();
    }

    @POST
    @Path("/{id}")
    public Response editTaskPost(@PathParam("id") int id,
                                 @FormParam("title") String title,
                                 @FormParam("description") String description,
                                 @FormParam("status") Integer status) {
        return editTask(id, title, description, status);
    }

    @PUT
    @Path("/{id}")
    public Response editTaskPut(@PathParam("id") int id,
                                @FormParam("title") String title,
                                @FormParam("description") String description,
                                @FormParam("status") Integer status) {
        return editTask(id, title, description, status);
    }

    private Response editTask(int id, String title, String description, Integer status) {
        Task task = new Task(id);
        if (!task.isLoaded()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Task not found")
                    .build();
        }

        task.setTitle(title != null ? title.trim() : task.getTitle());
        task.setDescription(description != null ? description.trim() : task.getDescription());
        if (status != null) {
            task.setStatus(TaskStatus.fromValue(status));
        }

        if (task.save()) {
            return Response.ok(1).build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Unable to update Task")
                .build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteTask(@PathParam("id") int id) {
        Task task = new Task(id);
        if (!task.isLoaded()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Task [" + id + "] not exists")
                    .build();
        }

        if (task.delete()) {
            return Response.ok(1).build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Unable to delete Task")
                .build();
    }

    @POST
    @Path("/user/{userId}/task/{taskId}")
    public Response addTaskToUserPost(@PathParam("userId") int userId,
                                      @PathParam("taskId") int taskId) {
        return addTaskToUser(userId, taskId);
    }

    @PUT
    @Path("/user/{userId}/task/{taskId}")
    public Response addTaskToUserPut(@PathParam("userId") int userId,
                                     @PathParam("taskId") int taskId) {
        return addTaskToUser(userId, taskId);
    }

    private Response addTaskToUser(int userId, int taskId) {
        User user = new User(userId);
        if (!user.isLoaded()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("User [" + userId + "] not exists")
                    .build();
        }

        Task task = new Task(taskId);
        if (!task.isLoaded()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Task [" + taskId + "] not exists")
                    .build();
        }

        UserTask userTask = new UserTask(userId);
        userTask.addTaskId(taskId);

        if (userTask.save()) {
            return Response.ok(1).build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Unable to add Task to user")
                .build();
    }

    @DELETE
    @Path("/user/{userId}/task/{taskId}")
    public Response removeTaskFromUser(@PathParam("userId") int userId,
                                       @PathParam("taskId") int taskId) {
        User user = new User(userId);
        if (!user.isLoaded()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("User [" + userId + "] not exists")
                    .build();
        }

        UserTask userTask = new UserTask(userId);

        if (userTask.hasTask(taskId)) {
            userTask.removeTaskId(taskId);
            if (userTask.save()) {
                return Response.ok(1).build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unable to delete Task of user")
                    .build();
        }

        return Response.ok(1).build();
    }
}
