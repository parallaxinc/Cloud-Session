# Cloud-Session
Session and Authentication system for cloud-compiled projects


## Manuals
### Building
As this is a maven project, all dependencies will be downloaded by maven. To create a war file execute **mvn clean package** in the root directory.

### Mysql setup
Create a schema and if wanted a user and import the [table definition](tables.sql).

### Configuration
Create a text file with the name **cloudsession.properties** and put in configurations as described in the [configuration manual](CONFIGURATION.md).

### Deployment
Set the generated or downloaded war into the webapps directory of your tomcat and start it.
