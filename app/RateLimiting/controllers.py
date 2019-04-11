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

import Failures
from app import db, app

from flask_restful import Resource, Api
from flask import request, Blueprint

from Validation import Validation

from app.User import services as user_services
from app.RateLimiting import services as rate_limiting_services

# from models import *

rate_limiting_app = Blueprint('rate', __name__, url_prefix='/bucket')
api = Api(rate_limiting_app)


class ConsumeSingle(Resource):

    def get(self, bucket_type, id_user):
        # Validate required fields
        validation = Validation()
        validation.add_required_field('bucket_type', bucket_type)
        validation.add_required_field('id_user', id_user)
        if not validation.is_valid():
            return validation.get_validation_response()


        # Parse numbers
        try:
            id_user = int(id_user)
        except:
            return Failures.not_a_number('idUser', id_user)

        # Validate user exists, is validated and is not blocked
        user = user_services.get_user(id_user)
        if user is None:
            return Failures.unknown_user_id(id_user)
        if user.blocked:
            return Failures.user_blocked()
        if not user.confirmed:
            return Failures.email_not_confirmed()

        bucket_types = app.config['CLOUD_SESSION_PROPERTIES']['bucket.types'].split(',')

        if bucket_type not in bucket_types:
            return Failures.unknown_bucket_type(bucket_type)

        # Decrement a token count
        result, next_time = rate_limiting_services.consume_tokens(user.id, bucket_type, 1)

        if not result:
            db.session.commit()
            return Failures.rate_exceeded(next_time.strftime("%Y-%m-%d %H:%M:%S"))

        db.session.commit()

        logging.info('RateLimiting-controller: ConsumeSingle: success: %s (%s)', id_user, bucket_type)

        return {'success': True}


class ConsumeMultiple(Resource):

    def get(self, bucket_type, id_user, count):
        # Validate required fields
        validation = Validation()
        validation.add_required_field('bucket_type', bucket_type)
        validation.add_required_field('id_user', id_user)
        validation.add_required_field('count', count)
        if not validation.is_valid():
            return validation.get_validation_response()

        # Parse numbers
        try:
            id_user = int(id_user)
        except ValueError:
            return Failures.not_a_number('idUser', id_user)

        try:
            count = int(count)
        except ValueError:
            return Failures.not_a_number('count', count)

        # Validate user exists, is validated and is not blocked
        user = user_services.get_user(id_user)

        if user is None:
            return Failures.unknown_user_id(id_user)
        if user.blocked:
            return Failures.user_blocked()
        if not user.confirmed:
            return Failures.email_not_confirmed()

        bucket_types = app.config['CLOUD_SESSION_PROPERTIES']['bucket.types'].split(',')

        if bucket_type not in bucket_types:
            return Failures.unknown_bucket_type(bucket_type)

        result, next_time = rate_limiting_services.consume_tokens(user.id, bucket_type, 1)

        if not result:
            db.session.commit()
            return Failures.rate_exceeded(next_time.strftime("%Y-%m-%d %H:%M:%S"))

        db.session.commit()

        logging.info('RateLimiting-controller: ConsumeMultiple: success: %s (%s - %s)', id_user, bucket_type, count)

        return {'success': True}


api.add_resource(ConsumeSingle, '/consume/<string:bucket_type>/<int:id_user>')
api.add_resource(ConsumeMultiple, '/consume/<string:bucket_type>/<int:id_user>/<int:count>')
