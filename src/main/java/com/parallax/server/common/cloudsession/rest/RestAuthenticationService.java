/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.rest;

import com.codahale.metrics.annotation.Timed;
import com.cuubez.visualizer.annotation.Detail;
import com.cuubez.visualizer.annotation.Group;
import com.cuubez.visualizer.annotation.HttpCode;
import com.cuubez.visualizer.annotation.Name;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.parallax.server.common.cloudsession.converter.UserConverter;
import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;
import com.parallax.server.common.cloudsession.db.utils.JsonResult;
import com.parallax.server.common.cloudsession.db.utils.Validation;
import com.parallax.server.common.cloudsession.exceptions.EmailNotConfirmedException;
import com.parallax.server.common.cloudsession.exceptions.InsufficientBucketTokensException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserException;
import com.parallax.server.common.cloudsession.exceptions.UserBlockedException;
import com.parallax.server.common.cloudsession.service.UserService;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 * @author Michel
 */
@Path("/authenticate")
@Group(name = "/authenticate", title = "Authentication services")
@HttpCode("500>Internal Server Error,200>Success Response")
public class RestAuthenticationService {

    private UserService userService;

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @POST
    @Path("/local")
    @Detail("Authenticate local user")
    @Name("Authenticate local")
    @Produces("text/json")
    @Timed(name = "authenticateLocalUser")
    public Response authenticateLocalUser(@FormParam("email") String email, @FormParam("password") String password) {
        Validation validation = new Validation();
        validation.addRequiredField("email", email);
        validation.addRequiredField("password", password);
        validation.checkEmail("email", email);
        if (!validation.isValid()) {
            return validation.getValidationResponse();
        }

        try {
            UserRecord user = userService.authenticateLocal(email, password);
            JsonObject json = new JsonObject();
            if (user != null) {
                if (user.getBlocked()) {
                    json.addProperty("success", false);
                    json.addProperty("message", "Account is blocked");
                    return Response.status(Response.Status.UNAUTHORIZED).entity(json.toString()).build();
                } else if (!user.getConfirmed()) {
                    json.addProperty("success", false);
                    json.addProperty("message", "Email not confirmed");
                    return Response.ok(json.toString()).build();
                } else {
                    json.addProperty("success", true);
                    json.add("user", UserConverter.toJson(user));
                    return Response.ok(json.toString()).build();
                }
            } else {
                json.addProperty("success", false);
                json.addProperty("message", "Unknown user");
                return Response.status(Response.Status.UNAUTHORIZED).entity(json.toString()).build();

            }
        } catch (UnknownUserException uue) {
            JsonObject json = new JsonObject();
            json.addProperty("success", false);
            json.addProperty("message", "Unknown user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(json.toString()).build();
        } catch (InsufficientBucketTokensException ibte) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(JsonResult.getFailure(ibte, "Password tries exceeded")).build();
        } catch (EmailNotConfirmedException ence) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(JsonResult.getFailure(ence)).build();
        } catch (UserBlockedException ube) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(JsonResult.getFailure(ube)).build();
        }
    }
}
