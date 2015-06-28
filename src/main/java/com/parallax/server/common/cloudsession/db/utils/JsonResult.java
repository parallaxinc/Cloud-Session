/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.utils;

import com.google.gson.JsonObject;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;

/**
 *
 * @author Michel
 */
public class JsonResult {

    public static String getFailure(String message) {
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.FALSE);
        result.addProperty("message", message);
        return result.toString();
    }

    public static String getFailure(String message, Object o) {
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.FALSE);
        result.addProperty("message", message);
        result.addProperty("data", String.valueOf(o));
        return result.toString();
    }

    public static String getFailure(UnknownUserException uue) {
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.FALSE);
        result.addProperty("message", uue.getMessage());
        result.addProperty("data", uue.getEmail());
        return result.toString();
    }

    public static String getFailure(UnknownUserIdException uuie) {
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.FALSE);
        result.addProperty("message", uuie.getMessage());
        result.addProperty("data", uuie.getIdUser());
        return result.toString();
    }

}
