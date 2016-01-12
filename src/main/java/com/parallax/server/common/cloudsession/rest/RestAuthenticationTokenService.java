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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.parallax.server.common.cloudsession.db.generated.tables.records.AuthenticationtokenRecord;
import com.parallax.server.common.cloudsession.db.utils.JsonResult;
import com.parallax.server.common.cloudsession.db.utils.Validation;
import com.parallax.server.common.cloudsession.exceptions.EmailNotConfirmedException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;
import com.parallax.server.common.cloudsession.exceptions.UserBlockedException;
import com.parallax.server.common.cloudsession.service.AuthenticationTokenService;
import com.parallax.server.common.cloudsession.service.ResetTokenService;
import com.parallax.server.common.cloudsession.service.UserService;
import java.util.List;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 * @author Michel
 */
@Path("/authtoken")
@Group(name = "/authtoken", title = "Authentication token services")
@HttpCode("500>Internal Server Error,200>Success Response")
public class RestAuthenticationTokenService {

    private ResetTokenService resetTokenService;

    private AuthenticationTokenService authenticationTokenService;

    private UserService userService;

    @Inject
    public void setResetTokenService(ResetTokenService resetTokenService) {
        this.resetTokenService = resetTokenService;
    }

    @Inject
    public void setAuthenticationTokenService(AuthenticationTokenService authenticationTokenService) {
        this.authenticationTokenService = authenticationTokenService;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @POST
    @Path("/request")
    @Detail("Requests an authentication token for the specified user")
    @Name("Request password reset")
    @Produces("text/json")
    @Timed(name = "requestResetById")
    public Response requestReset(@HeaderParam("server") String server, @FormParam("idUser") Long idUser, @FormParam("browser") String browser, @FormParam("ipAddress") String ipAddress) {
        Validation validation = new Validation();
        validation.addRequiredField("server", server);
        validation.addRequiredField("idUser", idUser);
        validation.addRequiredField("browser", browser);
        validation.addRequiredField("ipAddress", ipAddress);
        if (!validation.isValid()) {
            return validation.getValidationResponse();
        }

        try {
            AuthenticationtokenRecord authenticationtokenRecord = authenticationTokenService.createAuthenticationToken(server, idUser, browser, ipAddress);
            JsonObject json = new JsonObject();
            if (authenticationtokenRecord != null) {
                json.addProperty("success", true);
                json.addProperty("token", authenticationtokenRecord.getToken());
            } else {
                json.addProperty("success", false);
            }
            return Response.ok(json.toString()).build();
        } catch (UnknownUserIdException uuie) {
            return Response.serverError().entity(JsonResult.getFailure(uuie)).build();
        } catch (UserBlockedException ube) {
            return Response.serverError().entity(JsonResult.getFailure(ube)).build();
        } catch (EmailNotConfirmedException ence) {
            return Response.serverError().entity(JsonResult.getFailure(ence)).build();
        }
    }

    @POST
    @Path("/confirm")
    @Detail("Confirm the users authentication token")
    @Name("Do email confirm")
    @Produces("text/json")
    @Timed(name = "doConfirm")
    public Response doConfirm(@HeaderParam("server") String server, @FormParam("token") String token, @FormParam("idUser") Long idUser, @FormParam("browser") String browser, @FormParam("ipAddress") String ipAddress) {
        Validation validation = new Validation();
        validation.addRequiredField("server", server);
        validation.addRequiredField("idUser", idUser);
        validation.addRequiredField("token", token);
        validation.addRequiredField("browser", browser);
        validation.addRequiredField("ipAddress", ipAddress);
        if (!validation.isValid()) {
            return validation.getValidationResponse();
        }

        JsonObject json = new JsonObject();
        boolean valid = authenticationTokenService.isValidAuthenticationToken(token, server, idUser, browser, ipAddress);
        if (valid) {
            json.addProperty("success", true);
        } else {
            json.addProperty("success", false);
            json.addProperty("code", 510);
        }
        return Response.ok(json.toString()).build();
    }

    @POST
    @Path("/tokens/{id}")
    @Detail("Confirm the users authentication token")
    @Name("Do email confirm")
    @Produces("text/json")
    @Timed(name = "doConfirm")
    public Response getTokens(@HeaderParam("server") String server, @PathParam("id") Long idUser, @FormParam("browser") String browser, @FormParam("ipAddress") String ipAddress) {
        Validation validation = new Validation();
        validation.addRequiredField("server", server);
        validation.addRequiredField("id", idUser);
        validation.addRequiredField("browser", browser);
        validation.addRequiredField("ipAddress", ipAddress);
        if (!validation.isValid()) {
            return validation.getValidationResponse();
        }

        JsonArray json = new JsonArray();
        List<AuthenticationtokenRecord> authenticationTokens = authenticationTokenService.getValidAuthenticationTokens(server, idUser, browser, ipAddress);
        for (AuthenticationtokenRecord authenticationToken : authenticationTokens) {
            json.add(new JsonPrimitive(authenticationToken.getToken()));
        }

        return Response.ok(json.toString()).build();
    }

}
