/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.utils;

import com.google.common.base.Joiner;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michel
 */
public class Validation {

    private static final Logger log = LoggerFactory.getLogger(Validation.class);

    private List<String> requiredButMissingFields = new ArrayList<String>();
    private List<String> invalidEmailFields = new ArrayList<String>();

    public void addRequiredField(String field, Object object) {
        if (object == null) {
            log.warn("Missing required field: {}", field);
            requiredButMissingFields.add(field);
        }
    }

    public void checkEmail(String field, String email) {
        if (!isValidEmailAddress(email)) {
            log.warn("Invalid email address: {} (field: {})", email, field);
            invalidEmailFields.add(field);
        }
    }

    public boolean isValid() {
        boolean valid = true;
        if (requiredButMissingFields.size() > 0) {
            valid = false;
        }
        if (invalidEmailFields.size() > 0) {
            valid = false;
        }
        return valid;
    }

    public Response getValidationResponse() {
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.FALSE);
        result.addProperty("message", "Validation error");
        result.addProperty("code", 300);
        if (requiredButMissingFields.size() > 0) {
            result.addProperty("missing-fields", fieldList(requiredButMissingFields));
        }
        if (invalidEmailFields.size() > 0) {
            result.addProperty("invalid-email-fields", fieldList(invalidEmailFields));
        }
        return Response.status(Response.Status.BAD_REQUEST).entity(result.toString()).build();
    }

    // Util methods
    private static String fieldList(List<String> fieldNames) {
        return Joiner.on(", ").join(fieldNames);
    }

    // Static validation methods
    public static boolean isValidEmailAddress(String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }

}
