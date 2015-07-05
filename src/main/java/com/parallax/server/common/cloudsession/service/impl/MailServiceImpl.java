/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;
import com.parallax.server.common.cloudsession.service.MailService;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michel
 */
public class MailServiceImpl implements MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailServiceImpl.class);

    private Configuration configuration;

    @Inject
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void sendConfirmTokenEmail(String server, UserRecord user, String token) {
        LOG.info("send confirm-token email to {}", user.getEmail());
        String email = UrlEscapers.urlFragmentEscaper().escape(user.getEmail());
        String confirmUrl = "http://" + server + ".parallax.com/confirm/" + user.getLanguage() + "/" + email + "/" + token;
        StringBuilder builder = new StringBuilder();
        builder.append("Dear,\n\n").append("Please go to ").append(confirmUrl).append(" to confirm your email address.");
        builder.append("\n\nIf the url does not work please go to ").append("http://" + server + ".parallax.com/confirm/").append(" and enter ").append(token);
        builder.append("\n\n\nThe Parallax team");
        sendEmail(user.getEmail(), "Please confirm", builder.toString());
    }

    @Override
    public void sendResetTokenEmail(String server, UserRecord user, String token) {
        LOG.info("send reset-token email to {}", user.getEmail());
        String email = UrlEscapers.urlFragmentEscaper().escape(user.getEmail());
        String resetUrl = "http://" + server + ".parallax.com/reset/" + user.getLanguage() + "/" + email + "/" + token;
        StringBuilder builder = new StringBuilder();
        builder.append("Dear,\n\n").append("Please go to ").append(resetUrl).append(" to reset your password.");
        builder.append("\n\nIf the url does not work please go to ").append("http://" + server + ".parallax.com/reset/").append(" and enter ").append(token);
        builder.append("\n\n\nThe Parallax team");
        sendEmail(user.getEmail(), "Reset your password", builder.toString());
    }

    @Override
    public void sendEmail(String to, String subject, String message) {
        EmailSender emailSender = new EmailSender(to, subject, message);
        new Thread(emailSender).start();
    }

    private class EmailSender implements Runnable {

        private final String to;
        private final String subject;
        private final String message;

        public EmailSender(String to, String subject, String message) {
            this.to = to;
            this.subject = subject;
            this.message = message;
        }

        @Override
        public void run() {
            Properties properties = System.getProperties();
            properties.setProperty("mail.smtp.host", configuration.getString("mail.host", "localhost"));

            if (configuration.getBoolean("mail.tls", false)) {
                properties.setProperty("mail.smtp.starttls.enable", "true");
                properties.setProperty("mail.smtp.socketFactory.port", configuration.getString("mail.port", "587"));
                if (configuration.getBoolean("mail.ssl", false)) {
                    properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                }
                properties.put("mail.smtp.socketFactory.fallback", "false");
            }
            if (configuration.getBoolean("mail.authenticated", false)) {
                properties.setProperty("mail.smtp.auth", "true");
                properties.setProperty("mail.user", configuration.getString("mail.user"));
                properties.setProperty("mail.password", configuration.getString("mail.password"));
            }

            Session session = Session.getDefaultInstance(properties);
            try {
                MimeMessage mimeMessage = new MimeMessage(session);
                mimeMessage.setFrom(new InternetAddress(configuration.getString("mail.from", "noreply@example.com")));
                mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                mimeMessage.setSubject(subject);
                mimeMessage.setText(message);

                Transport.send(mimeMessage);
                LOG.info("Mail sent");
            } catch (MessagingException me) {
                me.printStackTrace();
            }
        }
    }

}
