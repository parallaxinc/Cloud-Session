/*
 * Add datestamps to the user record to track when the record was created
 * and when it was last modified.
 */
ALTER TABLE cloudsession.user ADD create_date DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL;
ALTER TABLE cloudsession.user ADD last_update DATETIME NULL ON UPDATE CURRENT_TIMESTAMP;
