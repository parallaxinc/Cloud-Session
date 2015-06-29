/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service;

import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;

/**
 *
 * @author Michel
 */
public interface MailService {

    void sendConfirmTokenEmail(String server, UserRecord user, String token);

    void sendResetTokenEmail(String server, UserRecord user, String token);

    void sendEmail(String to, String subject, String message);

}
