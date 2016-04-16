# Import flask and template operators

# Import properties files utils
import logging
from ConfigParser import ConfigParser

import os
from FakeSecHead import FakeSecHead
from os.path import expanduser, isfile

# Import Flask
from flask import Flask, render_template

# Import SQLAlchemy
from flask.ext.sqlalchemy import SQLAlchemy

# Import Mail
from flask.ext.mail import Mail

# Define the WSGI application object
from raven.contrib.flask import Sentry

app = Flask(__name__)

# Load basic configurations
app.config.from_object('config')

# --------------------------------------------- Application configurations --------------------------------------
defaults = {
            'database.url': 'mysql+mysqldb://cloudsession:cloudsession@localhost:3306/cloudsession',

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
            'bucket.password-reset.freq': '1800000',

            'bucket.email-confirm.size': '2',
            'bucket.email-confirm.input': '1',
            'bucket.email-confirm.freq': '1800000'
}

configfile = expanduser("~/cloudsession.properties")
print('Looking for config file: %s' % configfile)
if isfile(configfile):
    configs = ConfigParser(defaults)
    configs.readfp(FakeSecHead(open(configfile)))

    app_configs = {}
    for (key, value) in configs.items('section'):
        app_configs[key] = value
    app.config['CLOUD_SESSION_PROPERTIES'] = app_configs
else:
    app.config['CLOUD_SESSION_PROPERTIES'] = defaults


# -------------------------------------- Module initialization -------------------------------------------------
if app.config['CLOUD_SESSION_PROPERTIES']['sentry-dsn'] is not None:
    sentry = Sentry(app,
                    dsn=app.config['CLOUD_SESSION_PROPERTIES']['sentry-dsn'],
                    logging=True,
                    level=logging.ERROR
                    )


app.config['SQLALCHEMY_DATABASE_URI'] = app.config['CLOUD_SESSION_PROPERTIES']['database.url']

# Define the database object which is imported
# by modules and controllers
db = SQLAlchemy(app)

app.config['MAIL_SERVER'] = app.config['CLOUD_SESSION_PROPERTIES']['mail.host']
if app.config['CLOUD_SESSION_PROPERTIES']['mail.port'] is None:
    if app.config['CLOUD_SESSION_PROPERTIES']['mail.tls']:
        app.config['MAIL_PORT'] = 587
    else:
        app.config['MAIL_PORT'] = 25
else:
    app.config['MAIL_PORT'] = app.config['CLOUD_SESSION_PROPERTIES']['mail.port']
app.config['MAIL_USE_TLS'] = app.config['CLOUD_SESSION_PROPERTIES']['mail.tls']
app.config['MAIL_USE_SSL'] = app.config['CLOUD_SESSION_PROPERTIES']['mail.ssl']
app.config['MAIL_DEBUG'] = app.config['CLOUD_SESSION_PROPERTIES']['mail.debug']
app.config['MAIL_USERNAME'] = app.config['CLOUD_SESSION_PROPERTIES']['mail.user']
app.config['MAIL_PASSWORD'] = app.config['CLOUD_SESSION_PROPERTIES']['mail.password']
app.config['DEFAULT_MAIL_SENDER'] = app.config['CLOUD_SESSION_PROPERTIES']['mail.from']

#app.debug = True

mail = Mail(app)

# -------------------------------------------- Services --------------------------------------------------------
from app.AuthToken.controllers import auth_token_app
from app.Authenticate.controllers import authenticate_app
from app.User.controllers import user_app
from app.LocalUser.controllers import  local_user_app
from app.RateLimiting.controllers import rate_limiting_app

app.register_blueprint(auth_token_app)
app.register_blueprint(authenticate_app)
app.register_blueprint(user_app)
app.register_blueprint(local_user_app)
app.register_blueprint(rate_limiting_app)


# ------------------------------------------- Create DB --------------------------------------------------------
# Build the database:
# This will create the database file using SQLAlchemy
db.create_all()
