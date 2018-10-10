"""
Cloud Session server application initialization

"""

# Import properties files utils
import logging
from ConfigParser import ConfigParser
from FakeSecHead import FakeSecHead
from os.path import expanduser, isfile

# Import Flask
from flask import Flask

# Import SQLAlchemy database mapper
from flask_sqlalchemy import SQLAlchemy

# Import Mail
from flask_mail import Mail

# Define the WSGI application object
from raven.contrib.flask import Sentry

app = Flask(__name__)

# Application version (major,minor,patch-level)
version = "1.1.9"

"""
Change Log

1.1.9       Update failure module to include missing return elements.
            
1.1.8       Fail any attempt to reset an account password is the account
            email address has not yet been confirmed.

1.1.7       Update application logging to separate application events from
            those logged by the uwsgi servivce

1.1.6       Add email address detail for various authentication failures

1.1.5       Refactor _convert_email_uri(email) to properly handle a null
            email address.

1.1.4       Add code to convert plus signs located the the username portion
            of an email address to a '%2B'when the email address is embedded
            in a URL.
            
1.1.3       Added documentation around the user account registration process.

"""

db = None

# Load basic configurations
app.config.from_object('config')

# --------------------------------------------- Application configurations --------------------------------------
defaults = {
            'database.url': 'mysql+mysqldb://cloudsession:cloudsession@localhost:3306/cloudsession',

            'request.host': 'http://localhost:8080/blockly',
            'response.host': 'localhost',

            'sentry-dsn': None,

            'mail.host': 'localhost',
            'mail.port': None,
            'mail.from': 'noreply@example.com',
            'mail.user': None,
            'mail.password': None,
            'mail.tls': False,
            'mail.ssl': False,
            'mail.debug': app.debug,

            'confirm-token-validity-hours': '12',
            'reset-token-validity-hours': '12',

            'bucket.types': '',

            'bucket.failed-password.size': '3',
            'bucket.failed-password.input': '1',
            'bucket.failed-password.freq': '120000',

            'bucket.password-reset.size': '2',
            'bucket.password-reset.input': '1',
            'bucket.password-reset.freq': '600000',

            'bucket.email-confirm.size': '2',
            'bucket.email-confirm.input': '1',
            'bucket.email-confirm.freq': '1800000'
}


# Set up Cloud Session application log details. The user account that
# this application runs under must have create and write permissions to
# the /var/log/supervisor/ folder.
# ----------------------------------------------------------------------
logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(levelname)s %(message)s',
                    filename='/var/log/supervisor/cloud-session-app.log',
                    filemode='w')
logging.info('Log level set to %s', 'DEBUG')
logging.info('Starting Cloud Session Service v%s', version)

configfile = expanduser("~/cloudsession.properties")
logging.info('Looking for config file: %s', configfile)

if isfile(configfile):
    configs = ConfigParser(defaults)
    configs.readfp(FakeSecHead(open(configfile)))

    app_configs = {}
    logging.debug('Configuration Key Settings')
    for (key, value) in configs.items('section'):
        app_configs[key] = value
        logging.debug("Key:%s, Value:%s", key, value)

    app.config['CLOUD_SESSION_PROPERTIES'] = app_configs

else:
    app.config['CLOUD_SESSION_PROPERTIES'] = defaults
    logging.warn('WARNING: Using application defaults.')

# ----------  Init Sentry Module ----------
if app.config['CLOUD_SESSION_PROPERTIES']['sentry-dsn'] is not None:
    logging.info("Initializing Sentry")
    sentry = Sentry(app,
                    dsn=app.config['CLOUD_SESSION_PROPERTIES']['sentry-dsn'],
                    logging=True,
                    level=logging.ERROR
                    )
else:
    logging.info("No Sentry configuration")

# ----------  Init database package ----------
# Define the database object which is imported
# by modules and controllers
# --------------------------------------------
app.config['SQLALCHEMY_DATABASE_URI'] = app.config['CLOUD_SESSION_PROPERTIES']['database.url']
logging.debug("Initializing database connection: %s", app.config['SQLALCHEMY_DATABASE_URI'])

db = SQLAlchemy(app)


app.config['MAIL_SERVER'] = app.config['CLOUD_SESSION_PROPERTIES']['mail.host']
logging.debug("Configuring SMTP properties for %s", app.config['MAIL_SERVER'])

# Set the correct server port for encrypted vs unencrypted communications
if app.config['CLOUD_SESSION_PROPERTIES']['mail.port'] is None:
    if app.config['CLOUD_SESSION_PROPERTIES']['mail.tls']:
        app.config['MAIL_PORT'] = 587
    else:
        app.config['MAIL_PORT'] = 25

    logging.info("Email server default port set to port %s", app.config['MAIL_PORT'])
else:
    app.config['MAIL_PORT'] = app.config['CLOUD_SESSION_PROPERTIES']['mail.port']

app.config['MAIL_USE_TLS'] = app.config['CLOUD_SESSION_PROPERTIES']['mail.tls']
app.config['MAIL_USE_SSL'] = app.config['CLOUD_SESSION_PROPERTIES']['mail.ssl']
app.config['MAIL_DEBUG'] = app.config['CLOUD_SESSION_PROPERTIES']['mail.debug']
app.config['MAIL_USERNAME'] = app.config['CLOUD_SESSION_PROPERTIES']['mail.user']
app.config['MAIL_PASSWORD'] = app.config['CLOUD_SESSION_PROPERTIES']['mail.password']
app.config['DEFAULT_MAIL_SENDER'] = app.config['CLOUD_SESSION_PROPERTIES']['mail.from']


logging.info("Initializing mail")
logging.info("SMTP port: %s", app.config['MAIL_PORT'])
logging.info("TLS: %s",app.config['MAIL_USE_TLS'])
logging.info("SSL: %s",app.config['MAIL_USE_SSL'])
logging.info("Sender: %s",app.config['DEFAULT_MAIL_SENDER'])

mail = Mail(app)

# ---------- Services ----------
logging.info("Initializing services")

# All of these imports need the database
if db is not None:
    from app.Authenticate.controllers import authenticate_app
    from app.AuthToken.controllers import auth_token_app
    from app.User.controllers import user_app
    from app.LocalUser.controllers import  local_user_app
    from app.RateLimiting.controllers import rate_limiting_app
    from app.OAuth.controllers import oauth_app


app.register_blueprint(auth_token_app)
app.register_blueprint(authenticate_app)
app.register_blueprint(user_app)
app.register_blueprint(local_user_app)
app.register_blueprint(rate_limiting_app)
app.register_blueprint(oauth_app)


# ------------------------------------------- Create DB --------------------------------------------------------
# Build the database:
# This will create the database file using SQLAlchemy
logging.info("Creating database tables if required")
db.create_all()
