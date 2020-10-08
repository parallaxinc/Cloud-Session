# ------------------------------------------------------------------------------
#  Copyright (c) 2019 Parallax Inc.                                            -
#                                                                              -
#  Permission is hereby granted, free of charge, to any person obtaining       -
#  a copy of this software and associated documentation files (the             -
#  “Software”), to deal in the Software without restriction, including         -
#  without limitation the rights to use, copy,  modify, merge, publish,        -
#  distribute, sublicense, and/or sell copies of the Software, and to          -
#  permit persons to whom the Software is furnished to do so, subject          -
#  to the following conditions:                                                -
#                                                                              -
#     The above copyright notice and this permission notice shall be           -
#     included in all copies or substantial portions of the Software.          -
#                                                                              -
#  THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,             -
#  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF          -
#  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT.       -
#  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY        -
#  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,        -
#  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE           -
#  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                      -
#                                                                              -
#                                                                              -
# ------------------------------------------------------------------------------

"""
Cloud Session server application initialization

"""

# Get the application version
from sentry_sdk.integrations.flask import FlaskIntegration

from app import __version__

# Import properties files utils
import logging
from os.path import expanduser, isfile
from configparser import ConfigParser

# Import Flask
from flask import Flask

# Import SQLAlchemy database mapper
from flask_sqlalchemy import SQLAlchemy

# Import Mail
from flask_mail import Mail

import sentry_sdk


# ---------- Constants ----------
CONFIG_FILE = 'cloudsession.ini'
DEFAULT_LOG_PATH = '/var/log/supervisor/cloud-session-app.log'

# Define the WSGI application object
app = Flask(__name__)

# Application version (major,minor,patch-level)
# version has been moved to the __version__.py.
# Reference PEP 396 -- Module Version Numbers

db = None


# Load basic configurations
app.config.from_object('config')

# -------------------------- Application configurations -------------------------
# These settings are the default for those elements that are not defined in the
# user-supplied configuration file.
#
# The configuration file has the following sections:
#    [database]
#    [host]
#    [monitor]
#    [mail]
#    [tokens]
#    [buckets]
#
# -------------------------------------------------------------------------------
configDefaults = {
    # database
    'database.url': 'mysql+mysqldb://cloudsession_user:cloudsession_password@localhost:3306/cloudsession',

    # host
    'request.host': 'http://localhost:8080/blockly',
    'response.host': 'localhost',

    # monitor
    'sentry-dsn': '',

    # mail
    'mail.host': 'localhost',
    'mail.port': '',
    'mail.from': 'noreply@example.com',
    'mail.user': '',
    'mail.password': '',
    'mail.tls': '0',
    'mail.ssl': '0',
    'mail.debug': '0',

    # tokens
    'confirm-token-validity-hours': '12',
    'reset-token-validity-hours': '12',

    # buckets
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
                    filename=DEFAULT_LOG_PATH,
                    filemode='w')
logging.info('Log level set to %s', 'DEBUG')
logging.info('Starting Cloud Session Service v%s', __version__)


configFile = expanduser('~/' + CONFIG_FILE)
logging.info('Looking for config file: %s', configFile)

app_configs = {}
config = ConfigParser()
config['DEFAULT'] = configDefaults

if isfile(configFile):
    logging.info('Loading settings from %s.', configFile)

    # Load default settings first
    # configs = ConfigParser(configDefaults)

    # Load configuration file to override default settings
    config.read_file(open(configFile))
    logging.debug('Configuration Key Settings')

    # Load settings from the configuration file into a dictionary
    # for section in config.sections():
    for (key, value) in config.items('application', True):
        logging.debug("Key:%s, Value:%s", key, value)
        app_configs[key] = value

    logging.info("End of configuration list.")
    app.config['CLOUD_SESSION_PROPERTIES'] = app_configs

else:
    app.config['CLOUD_SESSION_PROPERTIES'] = configDefaults
    logging.warning('Using application defaults.')

# ----------  Init Sentry Module ----------
sentryDsn = app.config['CLOUD_SESSION_PROPERTIES']['sentry-dsn']
if sentryDsn is not None and sentryDsn != '':
    logging.info("Initializing Sentry")

    sentry_sdk.init(
        dsn="https://" +
            app.config['CLOUD_SESSION_PROPERTIES']['public_key'] +
            "@sentry.io/" +
            app.config['CLOUD_SESSION_PROPERTIES']['project'],
        integrations=[FlaskIntegration()])
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
logging.info("TLS: %s", app.config['MAIL_USE_TLS'])
logging.info("SSL: %s", app.config['MAIL_USE_SSL'])
logging.info("Sender: %s", app.config['DEFAULT_MAIL_SENDER'])

mail = Mail(app)

# ---------- Services ----------
logging.info("Initializing services")

# All of these imports need the database
if db is not None:
    from app.Authenticate.controllers import authenticate_app
    from app.AuthToken.controllers import auth_token_app
    from app.Health.controllers import health_app
    from app.User.controllers import user_app
    from app.LocalUser.controllers import local_user_app
    from app.RateLimiting.controllers import rate_limiting_app
    from app.OAuth.controllers import oauth_app

app.register_blueprint(auth_token_app)
app.register_blueprint(authenticate_app)
app.register_blueprint(health_app)
app.register_blueprint(user_app)
app.register_blueprint(local_user_app)
app.register_blueprint(rate_limiting_app)
app.register_blueprint(oauth_app)


# ------------------------------------------- Create DB --------------------------------------------------------
# Build the database:
# This will create the database file using SQLAlchemy
logging.info("Creating database tables if required")
# db.create_all()
