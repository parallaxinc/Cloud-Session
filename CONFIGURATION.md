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
To monitor execution times and counts metrics are collected during at runtime. These can be viewed/queried in multiple ways.

### Console
All collected metrics can be written to the console on regular basis. This can be configured with the following properties:

- Enable console reporting: **metrics.console.enable**, *true/false*. Defaults to: *false*
- Reporting frequency in seconds: **metrics.console.interval**. Defaults to: *30*

### Logger
These metrics can be logged through the logging framework.

- Enable logging based reporting: **metrics.logger.enable**, *true/false*. Defaults to: *false*
- Reporting frequency in seconds: **metrics.logger.interval**. Defaults to: *30*

### Graphite
The metrics can be sent to a graphite server, using the pickled-graphite format on port 2004.

- Enable graphite based reporting: **metrics.graphite.enable**, *true/false*. Defaults to: *false*
- Reporting frequency in seconds: **metrics.graphite.interval**. Defaults to: *30*
- Graphite server host: **metrics.graphite.host**. Defaults to *localhost*

### InfluxDb
The metrics can be sent to a InfluxDb server, using http transport.

- Enable influxdb based reporting: **metrics.influxdb.enable**, *true/false*. Defaults to: *false*
- Reporting frequency in seconds: **metrics.influxdb.interval**. Defaults to: *30*
- InfluxDb server host: **metrics.influxdb.host**. Defaults to *localhost*
- InfluxDb server port: **metrics.influxdb.port**. Defaults to *8086*
- InfluxDb server database: **metrics.influxdb.database**. Defaults to *mydb*
- InfluxDb server user: **metrics.influxdb.user**. Required to be set
- InfluxDb server password: **metrics.influxdb.password**. Required to be set

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

## Email template configuration
The email templates, created in [freemarker](http://freemarker.org/), have to be put in a specific directory sturcture, either on the hard-disk or available over the web.
The base path is configured as: **email.template.path**

This can be an absolute or relative path, or even an url. Please use forward slashes, even on a Windows computer.

Inside this base directory, following directory structure is required for the system to find the templates:

locale - type - server

Example (where *blocklyprop* is the server identifier for demonstration purposes):

- en
    - confirm
        - blocklyprop
    - reset
        - blocklyprop
- es
    - confirm
        - blocklyprop
    - reset
        - blocklyprop

In these directories a **header.ftl** and **plain.ftl** file are required. They will respectively contain the template for the email header and the plaintext html body.

The available placeholders are:

- **${email}** This will be replaced with the email address of the user
- **${locale}** This will be replaced with the locale of the user (will be the same as the name of the directory)
- **${screenname}** This will be replaced with the screenname of the user
- **${token}** This will be replaced with the token the user has to use for the specific operation

For more information about writing templates: see [the freemarker template Author's guide](http://freemarker.org/docs/dgui.html).

Email templates are cached, the settings for the cache are:

- Cache size (max amount of templates in the cache): **email.template.cache.size**. Defaults to: *500*
- Time to live (Time a template is cached, in seconds): **email.template.cache.ttl**. Defaults to: *300*