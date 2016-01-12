/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.config;

import com.parallax.server.common.cloudsession.rest.RestAuthenticationService;
import com.parallax.server.common.cloudsession.rest.RestAuthenticationTokenService;
import com.parallax.server.common.cloudsession.rest.RestBucketService;
import com.parallax.server.common.cloudsession.rest.RestLocalUserService;
import com.parallax.server.common.cloudsession.rest.RestUserService;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

/**
 *
 * @author Michel
 */
public class RestModule extends JerseyServletModule {

    @Override
    protected void configureServlets() {
        bind(RestAuthenticationService.class);
        bind(RestAuthenticationTokenService.class);
        bind(RestLocalUserService.class);
        bind(RestUserService.class);
        bind(RestBucketService.class);

        /* bind jackson converters for JAXB/JSON serialization */
        bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
        bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);

        serve("/rest/*").with(GuiceContainer.class);
    }

}
