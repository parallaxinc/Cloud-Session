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
import com.parallax.server.common.cloudsession.exceptions.NonUniqueEmailException;
import com.parallax.server.common.cloudsession.exceptions.PasswordComplexityException;
import com.parallax.server.common.cloudsession.exceptions.PasswordVerifyException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;
import com.parallax.server.common.cloudsession.service.UserService;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 * @author Michel
 */
@Path("/user")
@Group(name = "/user", title = "User services")
@HttpCode("500>Internal Server Error,200>Success Response")
public class RestUserService {

    private UserService userService;

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GET
    @Path("/email/{email}")
    @Detail("Get user data by email")
    @Name("Get user data by email")
    @Produces("text/json")
    @Timed(name = "getUserDataByEmail")
    public Response getUserDataByEmail(@PathParam("email") String email) {
        Validation validation = new Validation();
        validation.addRequiredField("email", email);
        validation.checkEmail("email", email);
        if (!validation.isValid()) {
            return validation.getValidationResponse();
        }

        try {
            UserRecord user = userService.getUser(email);
            JsonObject json = new JsonObject();
            json.addProperty("success", true);
            json.add("user", UserConverter.toJson(user));
            return Response.ok(json.toString()).build();
        } catch (UnknownUserException uue) {
            return Response.serverError().entity(JsonResult.getFailure(uue)).build();
        }
    }

    @GET
    @Path("/id/{id}")
    @Detail("Get user data by id")
    @Name("Get user data by id")
    @Produces("text/json")
    @Timed(name = "getUserDataById")
    public Response getUserDataById(@PathParam("id") Long id) {
        Validation validation = new Validation();
        validation.addRequiredField("id", id);
        if (!validation.isValid()) {
            return validation.getValidationResponse();
        }

        try {
            UserRecord user = userService.getUser(id);
            JsonObject json = new JsonObject();
            json.addProperty("success", true);
            json.add("user", UserConverter.toJson(user));
            return Response.ok(json.toString()).build();
        } catch (UnknownUserIdException uuie) {
            return Response.serverError().entity(JsonResult.getFailure(uuie)).build();
        }
    }

    @PUT
    @Path("/register")
    @Detail("Register new local user")
    @Name("Register")
    @Produces("text/json")
    @Timed(name = "register")
    public Response register(@HeaderParam("server") String server, @FormParam("email") String email, @FormParam("password") String password, @FormParam("password-confirm") String passwordConfirm, @FormParam("locale") String locale, @FormParam("screenname") String screenname) {
        Validation validation = new Validation();
        validation.addRequiredField("server", server);
        validation.addRequiredField("email", email);
        validation.addRequiredField("password", password);
        validation.addRequiredField("password-confirm", passwordConfirm);
        validation.addRequiredField("locale", locale);
        validation.addRequiredField("screenname", screenname);
        validation.checkEmail("email", email);
        if (!validation.isValid()) {
            return validation.getValidationResponse();
        }

        try {
            UserRecord user = userService.register(server, email, password, passwordConfirm, locale, screenname);
            JsonObject json = new JsonObject();
            if (user != null) {
                json.addProperty("success", true);
                json.addProperty("user", user.getId());
            } else {
                json.addProperty("success", false);
            }
            return Response.ok(json.toString()).build();
        } catch (NonUniqueEmailException nuie) {
            return Response.serverError().entity(JsonResult.getFailure(nuie)).build();
        } catch (PasswordVerifyException pve) {
            return Response.serverError().entity(JsonResult.getFailure(pve)).build();
        } catch (PasswordComplexityException pce) {
            return Response.serverError().entity(JsonResult.getFailure(pce)).build();
        }
    }

    @POST
    @Path("/info/{id}")
    @Detail("Change the users info")
    @Name("Do user info change")
    @Produces("text/json")
    @Timed(name = "doInfoChange")
    public Response doInfoChange(@PathParam("id") Long idUser, @FormParam("screenname") String screenname) {
        Validation validation = new Validation();
        validation.addRequiredField("id", idUser);
        validation.addRequiredField("screenname", screenname);
        if (!validation.isValid()) {
            return validation.getValidationResponse();
        }

        try {
            JsonObject json = new JsonObject();
            UserRecord userRecord = userService.changeInfo(idUser, screenname);
            if (userRecord != null) {
                json.addProperty("success", true);
                json.add("user", UserConverter.toJson(userRecord));
            } else {
                json.addProperty("success", false);
                json.addProperty("code", 530);
            }

            return Response.ok(json.toString()).build();
        } catch (UnknownUserIdException uue) {
            return Response.serverError().entity(JsonResult.getFailure(uue)).build();
        }
    }

    @POST
    @Path("/locale/{id}")
    @Detail("Change the users locale")
    @Name("Do user locale change")
    @Produces("text/json")
    @Timed(name = "doLocaleChange")
    public Response doLocaleChange(@PathParam("id") Long idUser, @FormParam("locale") String locale) {
        Validation validation = new Validation();
        validation.addRequiredField("id", idUser);
        validation.addRequiredField("locale", locale);
        if (!validation.isValid()) {
            return validation.getValidationResponse();
        }

        try {
            JsonObject json = new JsonObject();
            UserRecord userRecord = userService.changeLocale(idUser, locale);
            if (userRecord != null) {
                json.addProperty("success", true);
                json.add("user", UserConverter.toJson(userRecord));
            } else {
                json.addProperty("success", false);
                json.addProperty("code", 530);
            }

            return Response.ok(json.toString()).build();
        } catch (UnknownUserIdException uue) {
            return Response.serverError().entity(JsonResult.getFailure(uue)).build();
        }
    }

}
