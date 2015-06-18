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
import javax.ws.rs.GET;
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

    @GET
    @Path("/reset/{id}")
    @Detail("Requests to send a request email to the specified user with a password reset token")
    @Name("Reset")
    @Produces("text/json")
    public Response get(@PathParam("id") Long idUser) {
        JsonObject json = new JsonObject();
        json.addProperty("success", true);
        return Response.ok(json.toString()).build();
    }
}
