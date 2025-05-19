package com.sismics.docs.rest.resource;

import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.UserRequestDao;
import com.sismics.docs.core.model.jpa.UserRequest;
import com.sismics.docs.core.service.UserRequestService;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.docs.rest.resource.BaseResource;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/userrequest")
public class    UserRequestResource extends BaseResource {

    private UserRequestService userRequestService = new UserRequestService();
    private UserRequestDao userRequestDao = new UserRequestDao();

    /**
     * Create a user request.
     *
     * @api {put} /userrequest Register a new user request
     * @apiName PutUserRequest
     * @apiGroup UserRequest
     * @apiParam {String{3..50}} username Username
     * @apiParam {String{8..50}} password Password
     * @apiParam {String{1..100}} email E-mail
     * @apiSuccess {String} status Status OK
     * @apiError (client) ValidationError Validation error
     * @apiError (client) AlreadyExistingUsername Username already used
     * @apiVersion 1.5.0
     */
    @PUT
    public Response register(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("email") String email) {

        username = ValidationUtil.validateLength(username, "username", 3, 50);
        ValidationUtil.validateUsername(username, "username");
        password = ValidationUtil.validateLength(password, "password", 8, 50);
        email = ValidationUtil.validateLength(email, "email", 1, 100);
        ValidationUtil.validateEmail(email, "email");

        UserDao userDao = new UserDao();
        if (userDao.getActiveByUsername(username) != null) {
            throw new ClientException("AlreadyExistingUsername", "Username already used");
        }

        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(username);
        userRequest.setPassword(password);
        userRequest.setEmail(email);
        userRequest.setLocaleId("en");

        userRequestDao.create(userRequest);

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Create a user request.
     */
    @POST
    public Response create(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("email") String email) {

        if (username == null || username.isEmpty()) {
            throw new ClientException("ValidationError", "Username is required");
        }
        if (password == null || password.isEmpty()) {
            throw new ClientException("ValidationError", "Password is required");
        }
        if (email == null || email.isEmpty()) {
            throw new ClientException("ValidationError", "Email is required");
        }

        UserRequest userRequest = userRequestService.createRequest(username, password, email);

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok")
                .add("id", userRequest.getId())
                .add("username", userRequest.getUsername())
                .add("email", userRequest.getEmail())
                .add("create_date", userRequest.getCreateDate().getTime());
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Returns all pending user requests.
     *
     * @api {get} /userrequest List all pending requests
     * @apiName GetUserRequest
     * @apiGroup UserRequest
     * @apiSuccess {Object[]} requests List of requests
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission admin
     * @apiVersion 1.5.0
     */
    @GET
    public Response list() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        List<UserRequest> userRequests = userRequestDao.findAllPending();

        JsonArrayBuilder requests = Json.createArrayBuilder();
        for (UserRequest userRequest : userRequests) {
            requests.add(Json.createObjectBuilder()
                    .add("id", userRequest.getId())
                    .add("username", userRequest.getUsername())
                    .add("email", userRequest.getEmail())
                    .add("create_date", userRequest.getCreateDate().getTime()));
        }

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("requests", requests);
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Approve a user request.
     */
    @PUT
    @Path("{id:[a-z0-9\\\\-]+}/approve")
    public Response approve(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        try {
            UserRequest userRequest = userRequestService.approve(id, principal.getId());
            if (userRequest == null) {
                throw new ServerException("ApprovalError", "Error approving user request");
            }

            JsonObjectBuilder response = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("id", userRequest.getId())
                    .add("username", userRequest.getUsername())
                    .add("request_status", userRequest.getStatus());
            return Response.ok().entity(response.build()).build();
        } catch (IllegalStateException e) {
            throw new ClientException("InvalidRequest", e.getMessage());
        } catch (Exception e) {
            System.out.println("Error approving user request " + e);
            throw new ServerException("ApprovalError", "Error approving user request", e);
        }
    }


    /**
     * Reject a user request.
     */
    @PUT
    @Path("{id}/reject")
    public Response reject(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        UserRequest userRequest = userRequestService.reject(id, principal.getId());

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok")
                .add("id", userRequest.getId())
                .add("username", userRequest.getUsername())
                .add("request_status", userRequest.getStatus());
        return Response.ok().entity(response.build()).build();
    }
}