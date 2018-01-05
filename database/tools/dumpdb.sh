#!/bin/bash
#
# Dump the local cloudsession database to a .sql file
#

echo 'Enter the MySQL user account password below'
mysqldump --single-transaction -u blocklydb -p cloudsession > cs-backup.sql

