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

import logging
from flask_restful import Api, Resource
from flask import request, Blueprint
from app import __version__


# Set the base URL for this module and register it
health_app = Blueprint('health', __name__, url_prefix='/health')
api = Api(health_app)


class Ping(Resource):
    # noinspection PyUnresolvedReferences
    """
        Provide a simple response to verify that the Rest API is functioning.

        Args:
            None

        Returns:
            A JSON document with the key 'success' set to True, 'message' set to
            the constant 'pong', and a 200 response code.

        Raises:
            None
        """
    def get(self):
        # Ping the REST server for signs of life
        server = request.headers.get('server')
        logging.info("Requesting ping from server %s", server)

        return {
            'Success': True,
            'message': 'pong',
            'code': 200
        }


class Version(Resource):
    # noinspection PyUnresolvedReferences
    """
        Provide the application version string.

        Args:
            None

        Returns:
            A JSON document with the key 'success' set to True, 'message' contains a
            version element holding a string representation of the application version
            number, and a 200 response code.

        Raises:
            None
        """
    def get(self):
        # Ping the REST server for signs of life
        server = request.headers.get('server')
        logging.info("Requesting version info from server %s", server)

        return {
            'Success': True,
            'message': {
                'version': __version__.__version__,
            },
            'code': 200
        }


api.add_resource(Ping, '/ping')
api.add_resource(Version, '/version')