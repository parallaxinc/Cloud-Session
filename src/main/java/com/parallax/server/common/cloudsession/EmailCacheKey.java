/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession;

import java.util.Objects;

/**
 *
 * @author Michel
 */
public class EmailCacheKey {

    private String locale;
    private String server;
    private String type;
    private String part;

    public EmailCacheKey() {
    }

    public EmailCacheKey(String locale, String server, String type, String part) {
        this.locale = locale;
        this.server = server;
        this.type = type;
        this.part = part;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    @Override
    public String toString() {
        return "EmailCacheKey{" + "locale=" + locale + ", server=" + server + ", type=" + type + ", part=" + part + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + Objects.hashCode(this.locale);
        hash = 11 * hash + Objects.hashCode(this.server);
        hash = 11 * hash + Objects.hashCode(this.type);
        hash = 11 * hash + Objects.hashCode(this.part);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EmailCacheKey other = (EmailCacheKey) obj;
        if (!Objects.equals(this.locale, other.locale)) {
            return false;
        }
        if (!Objects.equals(this.server, other.server)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.part, other.part)) {
            return false;
        }
        return true;
    }

}
