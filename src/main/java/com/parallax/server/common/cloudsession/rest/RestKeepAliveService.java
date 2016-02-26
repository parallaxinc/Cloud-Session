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
import com.google.inject.Inject;
import com.parallax.server.common.cloudsession.db.utils.JsonResult;
import com.parallax.server.common.cloudsession.service.AuthenticationTokenService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 * @author Michel
 */
@Path("/keepalive")
@Group(name = "/keepalive", title = "Keep server/database connection alive")
@HttpCode("500>Internal Server Error,200>Success Response")
public class RestKeepAliveService {

    private AuthenticationTokenService authenticationTokenService;

    @Inject
    public void setAuthenticationTokenService(AuthenticationTokenService authenticationTokenService) {
        this.authenticationTokenService = authenticationTokenService;
    }

    @GET
    @Path("/do")
    @Detail("Keep alive")
    @Name("Keep alive")
    @Produces("text/json")
    public Response keepAlive() {
        int tokenCount = authenticationTokenService.cleanExpiredAutheticationTokens();
        return Response.ok(JsonResult.getSuccess(tokenCount)).build();
    }

}
