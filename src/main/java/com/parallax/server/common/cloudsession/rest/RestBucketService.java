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
import com.parallax.server.common.cloudsession.db.utils.JsonResult;
import com.parallax.server.common.cloudsession.db.utils.Validation;
import com.parallax.server.common.cloudsession.exceptions.InsufficientBucketTokensExceptions;
import com.parallax.server.common.cloudsession.exceptions.UnknownBucketTypeException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;
import com.parallax.server.common.cloudsession.service.BucketService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 * @author Michel
 */
@Path("/bucket")
@Group(name = "/bucket", title = "Leaky Bucket services")
@HttpCode("500>Internal Server Error,200>Success Response")
public class RestBucketService {

    private BucketService bucketService;

    @Inject
    public void setBucketService(BucketService bucketService) {
        this.bucketService = bucketService;
    }

    @GET
    @Path("/consume/{type}/{id}")
    @Detail("Consume one token of a given type for a given user")
    @Name("Consume one token")
    @Produces("text/json")
    public Response consumeOne(@PathParam("type") String type, @PathParam("id") Long id) {
        return consume(type, id, 1);
    }

    @GET
    @Path("/consume/{type}/{id}/{count}")
    @Detail("Consume a given amount of tokens of a given type for a given user")
    @Name("Consume x tokens")
    @Produces("text/json")
    public Response consume(@PathParam("type") String type, @PathParam("id") Long id, @PathParam("count") Integer count) {
        Validation validation = new Validation();
        validation.addRequiredField("type", type);
        validation.addRequiredField("id", id);
        validation.addRequiredField("count", count);
        if (!validation.isValid()) {
            return validation.getValidationResponse();
        }

        try {
            JsonObject json = new JsonObject();
            // Absolute value: negative values would always return true
            bucketService.consumeTokens(id, type, Math.abs(count));
            return Response.ok(JsonResult.getSuccess()).build();
        } catch (UnknownUserIdException uuie) {
            return Response.serverError().entity(JsonResult.getFailure(uuie)).build();
        } catch (UnknownBucketTypeException ubte) {
            return Response.serverError().entity(JsonResult.getFailure(ubte)).build();
        } catch (InsufficientBucketTokensExceptions ibte) {
            return Response.serverError().entity(JsonResult.getFailure(ibte)).build();
        }
    }
}
