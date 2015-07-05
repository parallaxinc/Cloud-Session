# Cloud-Session configuration

Database connection, metrics, mail server and token buckets can be configured using a properties file.
The application will look for a file called **cloudsession.properties** in the home directory of the user that started the tomcat at startup.

If changes are made to the configuration file, the server will need to be restarted to put these into effect.

## Database configuration

## Metrics

## Mail server

## Token buckets
The token buckets need to be listed and then configured one by one.

The list of bucket types is put in **bucket.types**, seperated by ', '.

Then each bucket needs some configuration, the size and input speed is required, while input frequency defaults to once a second.

The bucket size is put in **bucket.[TYPE].size** and the input speed (amount of tokens that are added based on the input frequency) in **bucket.[TYPE].input**.

If you want another frequency then onca a second, it can be configured in **bucket.[TYPE].freq**. The value is an amount of milliseconds that elaps between each time that new tokens are added into the bucket.
*Because of a limitation in the database, the timestamp of the last token change is limited to second resolution.*