from ConfigParser import ConfigParser

import json
from flask import Flask, Response, request
from flask_restful import Resource, Api
from os.path import expanduser, isfile

__author__ = 'Michel'

app = Flask(__name__)
api = Api(app)


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
    'c-compiler': '/opt/parallax/bin/propeller-elf-gcc',
    'c-libraries': '/opt/simple-libraries',
    'spin-compiler': '/opt/parallax/bin/openspin',
    'spin-libraries': '/opt/parallax/spin'
}

configfile = expanduser("~/cloudsession.properties")
if isfile(configfile):
    configs = ConfigParser(defaults)
    configs.readfp(FakeSecHead(open(configfile)))

    app_configs = {}
    for (key, value) in configs.items('section'):
        app_configs[key] = value
else:
    app_configs = defaults


# -------------------------------------------- Services --------------------------------------------------------
class RateLimiter(Resource):

    def get(self):
        return {'status': 'success'}

api.add_resource(RateLimiter, '/RateLimiter')

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

