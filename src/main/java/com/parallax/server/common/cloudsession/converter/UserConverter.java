/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.converter;

import com.google.gson.JsonObject;
import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;

/**
 *
 * @author Michel
 */
public class UserConverter {

    public static JsonObject toJson(UserRecord user) {
        JsonObject json = new JsonObject();
        json.addProperty("email", user.getEmail());
        json.addProperty("locale", user.getLocale());
        return json;
    }

}
