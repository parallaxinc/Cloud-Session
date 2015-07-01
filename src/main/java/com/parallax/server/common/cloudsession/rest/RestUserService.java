/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.rest;

import com.cuubez.visualizer.annotation.Detail;
import com.cuubez.visualizer.annotation.Group;
import com.cuubez.visualizer.annotation.HttpCode;
import com.cuubez.visualizer.annotation.Name;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;
import com.parallax.server.common.cloudsession.db.utils.JsonResult;
import com.parallax.server.common.cloudsession.db.utils.Validation;
import com.parallax.server.common.cloudsession.exceptions.NonUniqueEmailException;
import com.parallax.server.common.cloudsession.exceptions.PasswordVerifyException;
import com.parallax.server.common.cloudsession.service.UserService;
import javax.ws.rs.FormParam;
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

    @PUT
    @Path("/{server}")
    @Detail("Register new local user")
    @Name("Register")
    @Produces("text/json")
    public Response register(@PathParam("server") String server, @FormParam("email") String email, @FormParam("password") String password, @FormParam("password-confirm") String passwordConfirm, @FormParam("language") String language) {
        Validation validation = new Validation();
        validation.addRequiredField("server", server);
        validation.addRequiredField("email", email);
        validation.addRequiredField("password", password);
        validation.addRequiredField("password-confirm", passwordConfirm);
        validation.addRequiredField("language", language);
        validation.checkEmail("email", email);
        if (!validation.isValid()) {
            return validation.getValidationResponse();
        }

        try {
            UserRecord user = userService.register(server, email, password, passwordConfirm);
            JsonObject json = new JsonObject();
            if (user != null) {
                json.addProperty("success", true);
            } else {
                json.addProperty("success", false);
            }
            return Response.ok(json.toString()).build();
        } catch (NonUniqueEmailException nuie) {
            return Response.serverError().entity(JsonResult.getFailure(nuie)).build();
        } catch (PasswordVerifyException pve) {
            return Response.serverError().entity(JsonResult.getFailure(pve)).build();
        }
    }
}
