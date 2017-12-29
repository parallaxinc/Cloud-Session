/*
Script:  0004-update-cs-default-char-col.sql

This script corrects an issue in the cloud session database
where the default character set and collation were set to
'latin1' and 'latin1_swedish_ci'. These settings should be
'utf8' and 'utf8_general_ci'.

This script updates the character set and collation settings
on the cloudsession database, all cloudsession tables and
affected columns within each of these tables.
 */

# Select the target database
USE cloudsession;

# Set the database defaults
# This also sets the collation for individual table columns
ALTER DATABASE cloudsession CHARACTER SET utf8 COLLATE utf8_general_ci;

# latin1_general_ci
# ALTER DATABASE cloudsession CHARACTER SET latin1 COLLATE latin1_general_ci;

# Update the authentication_token table
SET foreign_key_checks = 0;
# Default table settings
ALTER TABLE cloudsession.authentication_token DEFAULT CHARACTER SET utf8;

# Column settings
ALTER TABLE cloudsession.authentication_token MODIFY browser VARCHAR(200) CHARACTER SET utf8;
ALTER TABLE cloudsession.authentication_token MODIFY server VARCHAR(1000) CHARACTER SET utf8;
ALTER TABLE cloudsession.authentication_token MODIFY ip_address VARCHAR(200) CHARACTER SET utf8;
ALTER TABLE cloudsession.authentication_token MODIFY token VARCHAR(200) CHARACTER SET utf8;

SET foreign_key_checks = 1;


# Update the bucket table
SET foreign_key_checks = 0;
ALTER TABLE cloudsession.bucket DEFAULT CHARACTER SET utf8;

# Reset fields
ALTER TABLE cloudsession.bucket MODIFY type VARCHAR(200) CHARACTER SET utf8;

SET foreign_key_checks = 1;


# Update the confirm_token table
SET foreign_key_checks = 0;
ALTER TABLE cloudsession.confirm_token DEFAULT CHARACTER SET utf8;

# Reset fields
ALTER TABLE cloudsession.confirm_token MODIFY token VARCHAR(200) CHARACTER SET utf8;

SET foreign_key_checks = 1;


# Update the reset_token table
SET foreign_key_checks = 0;
ALTER TABLE cloudsession.reset_token DEFAULT CHARACTER SET utf8;

# Reset fields
ALTER TABLE cloudsession.reset_token MODIFY token VARCHAR(200) CHARACTER SET utf8;

SET foreign_key_checks = 1;


# Update the user table
SET foreign_key_checks = 0;
ALTER TABLE cloudsession.user DEFAULT CHARACTER SET utf8;

# Reset fields
ALTER TABLE cloudsession.user MODIFY email VARCHAR(250) CHARACTER SET utf8;
ALTER TABLE cloudsession.user MODIFY password VARCHAR(100) CHARACTER SET utf8;
ALTER TABLE cloudsession.user MODIFY salt VARCHAR(50) CHARACTER SET utf8;
ALTER TABLE cloudsession.user MODIFY auth_source VARCHAR(250) CHARACTER SET utf8;
ALTER TABLE cloudsession.user MODIFY locale VARCHAR(50) CHARACTER SET utf8;
ALTER TABLE cloudsession.user MODIFY screen_name VARCHAR(250) CHARACTER SET utf8;
ALTER TABLE cloudsession.user MODIFY parent_email VARCHAR(250) CHARACTER SET utf8;

SET foreign_key_checks = 1;

