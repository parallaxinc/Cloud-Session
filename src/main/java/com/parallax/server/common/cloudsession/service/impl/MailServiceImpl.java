/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.google.code.simplelrucache.ConcurrentLruCache;
import com.google.code.simplelrucache.LruCache;
import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import com.parallax.server.common.cloudsession.EmailCacheKey;
import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;
import com.parallax.server.common.cloudsession.service.MailService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michel
 */
public class MailServiceImpl implements MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailServiceImpl.class);

    private org.apache.commons.configuration.Configuration configuration;

    private Configuration cfg;
    private LruCache<EmailCacheKey, Template> templateCache;

    public MailServiceImpl() {
        cfg = new Configuration();

        // Set the preferred charset template files are stored in. UTF-8 is
        // a good choice in most applications:
        cfg.setDefaultEncoding("UTF-8");

        // Sets how errors will appear.
        // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    @Inject
    public void setConfiguration(org.apache.commons.configuration.Configuration configuration) {
        this.configuration = configuration;
        try {
            // Specify the source where the template files come from. Here I set a
            // plain directory for it, but non-file-system sources are possible too:
            String basePath = configuration.getString("email.template.path", "templates");
            File baseDirectory = new File(basePath);

            if (baseDirectory.exists() && baseDirectory.isDirectory()) {
                System.out.println("baseDirectory: " + baseDirectory.getAbsolutePath());
                cfg.setDirectoryForTemplateLoading(new File(basePath));
            } else {
                System.out.println("baseDirectory: " + baseDirectory.getAbsolutePath() + " doesn't exist or is not a directory");
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MailServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        templateCache = new ConcurrentLruCache<>(configuration.getInt("email.template.cache.size", 500), configuration.getInt("email.template.cache.ttl", 300) * 1000);
    }

    @Override
    public void sendConfirmTokenEmail(String server, UserRecord user, String token) {
        LOG.info("send confirm-token email to {}", user.getEmail());

        Map<String, Object> data = new HashMap<>();
        data.put("email", UrlEscapers.urlFragmentEscaper().escape(user.getEmail()).replace("+", "%2B"));
        data.put("locale", user.getLocale());
        data.put("screenname", user.getScreenname());
        data.put("token", token);

        Template plainTemplate = getTemplate(server, user.getLocale(), "confirm", "plain");
        Template headerTemplate = getTemplate(server, user.getLocale(), "confirm", "header");

        StringWriter plain = new StringWriter();
        StringWriter header = new StringWriter();
        if (plainTemplate != null) {

            try {
                plainTemplate.process(data, plain);
            } catch (TemplateException ex) {
                java.util.logging.Logger.getLogger(MailServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(MailServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            plain.write("Please use " + token + " to confirm your email address");
        }
        if (headerTemplate != null) {
            try {
                headerTemplate.process(data, header);
            } catch (TemplateException ex) {
                java.util.logging.Logger.getLogger(MailServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(MailServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            header.write("Please confirm");
        }

//        String email = UrlEscapers.urlFragmentEscaper().escape(user.getEmail());
//        String confirmUrl = "http://localhost:8080/" + server + "/confirm?lang=" + user.getLocale() + "&email=" + email + "&token=" + token;
//        StringBuilder builder = new StringBuilder();
//        builder.append("Dear,\n\n").append("Please go to ").append(confirmUrl).append(" to confirm your email address.");
//        builder.append("\n\nIf the url does not work please go to ").append("http://localhost:8080/" + server + "/confirm/").append(" and enter ").append(token);
//        builder.append("\n\n\nThe Parallax team");
        sendEmail(user.getEmail(), header.toString(), plain.toString());
    }

    @Override
    public void sendResetTokenEmail(String server, UserRecord user, String token) {
        LOG.info("send reset-token email to {}", user.getEmail());

        Map<String, Object> data = new HashMap<>();
        data.put("email", UrlEscapers.urlFragmentEscaper().escape(user.getEmail()).replace("+", "%2B"));
        data.put("locale", user.getLocale());
        data.put("screenname", user.getScreenname());
        data.put("token", token);

        Template plainTemplate = getTemplate(server, user.getLocale(), "reset", "plain");
        Template headerTemplate = getTemplate(server, user.getLocale(), "reset", "header");

        StringWriter plain = new StringWriter();
        StringWriter header = new StringWriter();
        if (plainTemplate != null) {

            try {
                plainTemplate.process(data, plain);
            } catch (TemplateException ex) {
                LOG.error("Template exception", ex);
            } catch (IOException ex) {
                LOG.error("IO exception", ex);
            }
        } else {
            plain.write("Please use " + token + " to confirm your email address");
        }
        if (headerTemplate != null) {
            try {
                headerTemplate.process(data, header);
            } catch (TemplateException ex) {
                LOG.error("Template exception", ex);
            } catch (IOException ex) {
                LOG.error("IO exception", ex);
            }
        } else {
            header.write("Please confirm");
        }

//        String email = UrlEscapers.urlFragmentEscaper().escape(user.getEmail());
//        String resetUrl = "http://localhost:8080/" + server + "/reset?lang=" + user.getLocale() + "&email=" + email + "&token=" + token;
//        StringBuilder builder = new StringBuilder();
//        builder.append("Dear,\n\n").append("Please go to ").append(resetUrl).append(" to reset your password.");
//        builder.append("\n\nIf the url does not work please go to ").append("http://localhost:8080/" + server + "/reset/").append(" and enter ").append(token);
//        builder.append("\n\n\nThe Parallax team");
        // sendEmail(user.getEmail(), "Reset your password", builder.toString());
        sendEmail(user.getEmail(), header.toString(), plain.toString());
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

            if (configuration.getBoolean("mail.debug", false)) {
                properties.put("mail.debug", "true");
            }

            Session session = null;
            if (configuration.getBoolean("mail.authenticated", false)) {
                properties.setProperty("mail.smtp.auth", "true");
                session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(configuration.getString("mail.user"), configuration.getString("mail.password"));
                    }

                });
            } else {
                session = Session.getDefaultInstance(properties);
            }

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

    private Template getTemplate(String server, String locale, String type, String part) {
        EmailCacheKey emailCacheKey = new EmailCacheKey(locale, server, type, part);
        if (templateCache.contains(emailCacheKey)) {
            return templateCache.get(emailCacheKey);
        }
        String templateLocation = getTemplateLocation(server, locale, type, part);
        try {
            Template template = cfg.getTemplate(templateLocation, LocaleUtils.toLocale(locale));
            templateCache.put(emailCacheKey, template);
            return template;
        } catch (IOException ex) {
            LOG.error("IOException while loading template", ex);
        }
        return null;
    }

    private String getTemplateLocation(String server, String locale, String type, String part) {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(locale).append("/").append(type).append("/").append(server).append("/").append(part).append(".ftl");
        String template = pathBuilder.toString();
        System.out.println("Template location: " + template);
        return template;
    }

}
