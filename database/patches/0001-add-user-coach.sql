/*
 * Add coach email address field to support email cc option.
 */
USE cloudsession;
ALTER TABLE cloudsession.user ADD COLUMN coach_email VARCHAR(250) AFTER screen_name;
