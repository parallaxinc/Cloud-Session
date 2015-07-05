# Cloud-Session configuration

Database connection, metrics, mail server and token buckets can be configured using a properties file.
The application will look for a file called **cloudsession.properties** in the home directory of the user that started the tomcat at startup.

If changes are made to the configuration file, the server will need to be restarted to put these into effect.

## Database configuration
To configure the database connection use the following configuration properties:

- Java driver for the database: **database.driver**. Defaults to: *com.mysql.jdbc.Driver*
- Connection url: **database.url**. Defaults to: *jdbc:mysql://localhost:3306/cloudsession*
- Connection username: **database.username**. Defaults to: *cloudsession*
- Connection password: **database.password**. Defaults to: *cloudsession*
- Database dialect: **database.dialect**. Defaults to: *MYSQL*

To use anything but mysql, the driver library will need to be added to the java classpath. The dialect needs to be one of the ones listed on [the jooq sql dialects](http://www.jooq.org/javadoc/3.6.x/org/jooq/SQLDialect.html).
It's possible that for some types of databases a license is required, see [the jooq pricing page](http://www.jooq.org/download/).

## Metrics

## Mail server
By default mails will be sent from *noreply@example.com* using a smtp server on localhost with a non encrypted connection on port 25. For any changes use the following configurations.

- Send mail from a given address: **mail.from**. Defaults to: *noreply@example.com*
- Set mailserver host: **mail.host**. Defaults to: *localhost*
- Enable authentication on the mailserver: **mail.authenticated**, *true/false*. Defaults to: *false*
- Set username for authentication on the mailserver: **mail.user**. Required when **mail.authenticated** is set to *true*
- Set password for authentication on the mailserver: **mail.password**. Required when **mail.authenticated** is set to *true*
- Enable tls encryption on the connection: **mail.tls**, *true/false*. Defaults to: *false*. Setting this to true will change the default port to 587.
- Change the connection port (only available when tls is enabled): **mail.port**. Defaults to *587*
- Enable ssl encryption on the connection (only available when tls is enabled): **mail.ssl**, *true/false*. Defaults to: *false*

## Token buckets
The token buckets need to be listed and then configured one by one.

The list of bucket types is put in **bucket.types**, seperated by ', '.

Then each bucket needs some configuration, the size and input speed is required, while input frequency defaults to once a second. In each of these configurations, replace the **[TYPE]** by the string previously put in the **bucket.types** parameter.

The bucket size is put in **bucket.[TYPE].size** and the input speed (amount of tokens that are added based on the input frequency) in **bucket.[TYPE].input**.

If you want another frequency then onca a second, it can be configured in **bucket.[TYPE].freq**. The value is an amount of milliseconds that elaps between each time that new tokens are added into the bucket.

*Because of a limitation in the database, the timestamp of the last token change is limited to second resolution.*

*Each of the bucket configurations has to be a positive number of no more then 2,000,000,000 (about 24 days), and cannot have decimal places.*

## Wrong password, email confirm and password reset policies
Token buckets are used to configure rate limitting on logins with wrong passwords, email confirm requests and password reset requests.

To change the default values for these limits use the configuration as described in the token buckets section.

- Wrong password (use type: **failed-password**): 1 token every 120000ms (2 minutes) with a bucket size of 3.
- Email confirm requests (use type: **email-confirm**): 1 token every 1800000ms (30 minutes) with a bucket size of 2.
- Password reset requests (use type: **password-reset**): 1 token every 1800000ms (30 minutes) with a bucket size of 2.