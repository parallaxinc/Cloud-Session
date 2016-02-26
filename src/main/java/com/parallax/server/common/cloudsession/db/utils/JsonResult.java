/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.utils;

import com.google.gson.JsonObject;
import com.parallax.server.common.cloudsession.exceptions.EmailNotConfirmedException;
import com.parallax.server.common.cloudsession.exceptions.InsufficientBucketTokensException;
import com.parallax.server.common.cloudsession.exceptions.NonUniqueEmailException;
import com.parallax.server.common.cloudsession.exceptions.PasswordComplexityException;
import com.parallax.server.common.cloudsession.exceptions.PasswordVerifyException;
import com.parallax.server.common.cloudsession.exceptions.ScreennameUsedException;
import com.parallax.server.common.cloudsession.exceptions.UnknownBucketTypeException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;
import com.parallax.server.common.cloudsession.exceptions.UserBlockedException;
import java.text.SimpleDateFormat;

/**
 *
 * @author Michel
 */
public class JsonResult {

    private static SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static String getSuccess() {
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.TRUE);
        return result.toString();
    }

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
        result.addProperty("code", 400);
        return result.toString();
    }

    public static String getFailure(UnknownUserIdException uuie) {
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.FALSE);
        result.addProperty("message", uuie.getMessage());
        result.addProperty("data", uuie.getIdUser());
        result.addProperty("code", 400);
        return result.toString();
    }

    public static String getFailure(NonUniqueEmailException nuie) {
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.FALSE);
        result.addProperty("message", nuie.getMessage());
        result.addProperty("data", nuie.getEmail());
        result.addProperty("code", 450);
        return result.toString();
    }

    public static String getFailure(PasswordVerifyException pve) {
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.FALSE);
        result.addProperty("message", pve.getMessage());
        result.addProperty("code", 460);
        return result.toString();
    }

    public static String getFailure(UnknownBucketTypeException ubte) {
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.FALSE);
        result.addProperty("message", ubte.getMessage());
        result.addProperty("data", ubte.getType());
        result.addProperty("code", 480);
        return result.toString();
    }

    public static String getFailure(InsufficientBucketTokensException ibte) {
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.FALSE);
        result.addProperty("message", ibte.getMessage());
        result.addProperty("data", DATE_TIME_FORMATTER.format(ibte.getNextTime()));
        result.addProperty("code", 470);
        return result.toString();
    }

    public static String getFailure(InsufficientBucketTokensException ibte, String message) {
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.FALSE);
        result.addProperty("message", message);
        result.addProperty("data", DATE_TIME_FORMATTER.format(ibte.getNextTime()));
        result.addProperty("code", 470);
        return result.toString();
    }

    public static String getFailure(EmailNotConfirmedException ence) {
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.FALSE);
        result.addProperty("message", ence.getMessage());
        result.addProperty("code", 430);
        return result.toString();
    }

    public static String getFailure(UserBlockedException ube) {
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.FALSE);
        result.addProperty("message", ube.getMessage());
        result.addProperty("code", 420);
        return result.toString();
    }

    public static String getFailure(PasswordComplexityException pce) {
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.FALSE);
        result.addProperty("message", pce.getMessage());
        result.addProperty("code", 490);
        return result.toString();
    }

    public static String getFailure(ScreennameUsedException sue) {
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.FALSE);
        result.addProperty("message", sue.getMessage());
        result.addProperty("data", sue.getScreenname());
        result.addProperty("code", 500);
        return result.toString();
    }

    public static String getValidationFailure(String requiredFields, boolean validEmailAddress) {
        if (requiredFields == null && validEmailAddress) {
            return null;
        }
        JsonObject result = new JsonObject();
        result.addProperty("success", Boolean.FALSE);
        result.addProperty("message", "validation failed");
        if (requiredFields != null) {
            result.addProperty("required-fields", requiredFields);
        }
        if (!validEmailAddress) {
            result.addProperty("email", "invalid");
        }
        return result.toString();
    }

}
