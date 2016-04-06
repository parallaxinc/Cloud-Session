from ConfigParser import ConfigParser

import json
from flask import Flask, Response, request
from flask_restful import Resource, Api
from flask_sqlalchemy import SQLAlchemy
from os.path import expanduser, isfile

import AuthTokensService

__author__ = 'Michel'

app = Flask(__name__)

# ------------------------------------- Util functions and classes -------------------------------------------
class FakeSecHead(object):
    def __init__(self, fp):
        self.fp = fp
        self.sec_head = '[section]\n'

    def readline(self):
        if self.sec_head:
            try:
                return self.sec_head
            finally:
                self.sec_head = None
        else:
            return self.fp.readline()

# --------------------------------------------- Application configurations --------------------------------------
defaults = {
            'database.url': 'mysql+mysqldb://cloudsession:cloudsession@localhost:3306/cloudsession',

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
app.config['SQLALCHEMY_DATABASE_URI'] = app.config['CLOUD_SESSION_PROPERTIES']['database.url']
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
#app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///db.sqlite'
db = SQLAlchemy(app)

import models

api = Api(app)

# -------------------------------------------- Services --------------------------------------------------------
class RateLimiter(Resource):

    def get(self):
        return {'status': 'success'}

api.add_resource(RateLimiter, '/RateLimiter')
api.add_resource(AuthTokensService.AuthTokensRequest, '/authtoken/request')

# -------------------------------------------- Logging ---------------------------------------------------------
if not app.debug:
    import logging
    from logging.handlers import RotatingFileHandler
    file_handler = RotatingFileHandler('cloudsession.log')
    file_handler.setLevel(logging.WARNING)
    file_handler.setFormatter(logging.Formatter(
        '%(asctime)s %(levelname)s: %(message)s '
        '[in %(pathname)s:%(lineno)d]'
    ))
    app.logger.addHandler(file_handler)


# ----------------------------------------------- Development server -------------------------------------------
if __name__ == '__main__':
    app.debug = True
    app.run(host='0.0.0.0')

