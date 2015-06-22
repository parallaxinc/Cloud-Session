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
import com.parallax.server.common.cloudsession.db.generated.tables.records.ResettokenRecord;
import com.parallax.server.common.cloudsession.service.ResetTokenService;
import com.parallax.server.common.cloudsession.service.UserService;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 * @author Michel
 */
@Path("/local")
@Group(name = "/local", title = "Local user services using tokens")
@HttpCode("500>Internal Server Error,200>Success Response")
public class RestLocalUserServices {

    private ResetTokenService resetTokenService;

    private UserService userService;

    @Inject
    public void setResetTokenService(ResetTokenService resetTokenService) {
        this.resetTokenService = resetTokenService;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GET
    @Path("/reset/{id}")
    @Detail("Requests to send a request email to the specified user with a password reset token")
    @Name("Reset")
    @Produces("text/json")
    public Response requestReset(@PathParam("id") Long idUser) {
        ResettokenRecord resetToken = resetTokenService.createResetToken(idUser);
        JsonObject json = new JsonObject();
        if (resetToken != null) {
            json.addProperty("success", true);
            json.addProperty("token", resetToken.getToken());
        } else {
            json.addProperty("success", false);
        }
        return Response.ok(json.toString()).build();
    }

    @POST
    @Path("/reset/{id}")
    @Detail("Requests to send a request email to the specified user with a password reset token")
    @Name("Reset")
    @Produces("text/json")
    public Response doReset(@PathParam("id") Long idUser, @FormParam("token") String token, @FormParam("password") String password, @FormParam("password-confirm") String passwordConfirm) {
        boolean validResetToken = resetTokenService.isValidResetToken(token);
        if (validResetToken) {

        }
        JsonObject json = new JsonObject();
        json.addProperty("success", true);
        return Response.ok(json.toString()).build();
    }
}
