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

import datetime
import logging

import Failures
from app import db, app


from flask_restful import Resource, Api
from flask import request, Blueprint

from Validation import Validation

from app.User import services as user_service
from app.User.models import ConfirmToken, ResetToken

oauth_app = Blueprint('oauth', __name__, url_prefix='/oauth')
api = Api(oauth_app)


class ValidateUser(Resource):

    def post(self):
        # Get values
        server = request.headers.get('server')
        email = request.form.get('email')
        source = request.form.get('source')

        # Validate required fields
        validation = Validation()
        validation.add_required_field('server', server)
        validation.add_required_field('email', email)
        validation.add_required_field('source', source)
        validation.check_email('email', email)
        if not validation.is_valid():
            return validation.get_validation_response()

        # Validate user exits
        user = user_service.get_user_by_email(email)
        if user is None:
            return Failures.unknown_user_email(email)

        # Validate auth source
        if user.auth_source != source:
            return Failures.wrong_auth_source(user.auth_source)

        logging.info('OAuth-controller: Validate: success: %s', user.id)

        return {'success': True, 'user': {
            'id': user.id,
            'email': user.email,
            'locale': user.locale,
            'screenname': user.screen_name
        }}


class CreateUser(Resource):

    def post(self):
        # Get values
        server = request.headers.get('server')
        email = request.form.get('email')
        locale = request.form.get('locale')
        screen_name = request.form.get('screenname')
        source = request.form.get('source')

        # Validate required fields
        validation = Validation()
        validation.add_required_field('server', server)
        validation.add_required_field('email', email)
        validation.add_required_field('locale', locale)
        validation.add_required_field('screenname', screen_name)
        validation.add_required_field('source', source)
        validation.check_email('email', email)
        if not validation.is_valid():
            return validation.get_validation_response()

        # Validate email is not yet used
        existing_user = user_service.get_user_by_email(email)
        if existing_user is not None:
            return Failures.email_already_in_use(email)

        # Validate screen name is not yet used
        existing_user = user_service.get_user_by_screen_name(screen_name)
        if existing_user is not None:
            return Failures.screen_name_already_in_use(screen_name)

        id_user = user_service.create_oauth_user(server, email, source, locale, screen_name)

        db.session.commit()

        logging.info('OAuth-controller: create success: %s', id_user)

        # Create user
        return {'success': True, 'user': id_user}


api.add_resource(ValidateUser, '/validate')
api.add_resource(CreateUser, '/create')
