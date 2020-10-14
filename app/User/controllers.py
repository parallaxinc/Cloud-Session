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
from app import db


from flask_restful import Resource, Api
from flask import request, Blueprint
from Validation import Validation
from app.User import services as user_service

# Define the endpoint prefix for user services
user_app = Blueprint('user', __name__, url_prefix='/user')
api = Api(user_app)


# Register a new user
class Register(Resource):

    @staticmethod
    def post():
        # Get values
        server = request.headers.get('server')
        email = request.form.get('email')
        password = request.form.get('password')
        password_confirm = request.form.get('password-confirm')
        locale = request.form.get('locale')
        screen_name = request.form.get('screenname')

        # COPPA support
        birth_month = request.form.get('bdmonth')
        birth_year = request.form.get('bdyear')
        parent_email = request.form.get('parent-email')
        parent_email_source = request.form.get('parent-email-source')

        # Validate required fields
        validation = Validation()
        validation.add_required_field('server', server)
        validation.add_required_field('email', email)
        validation.add_required_field('password', password)
        validation.add_required_field('password-confirm', password_confirm)
        validation.add_required_field('locale', locale)
        validation.add_required_field('screenname', screen_name)

        # COPPA support
        validation.add_required_field('bdmonth', birth_month)
        validation.add_required_field('bdyear', birth_year)
        if parent_email:
            validation.check_email('parent-email', parent_email)
            if not validation.is_valid():
                return validation.get_validation_response()

        # Verify user email address
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

        # Validate password strength and confirm
        if password != password_confirm:
            return Failures.passwords_do_not_match()
        if not user_service.check_password_complexity(password):
            return Failures.password_complexity()

        # Write user details to the database
        id_user = user_service.create_local_user(
            server, email, password, locale, screen_name,
            birth_month, birth_year, parent_email, parent_email_source)

        # Send a confirmation request email to user or parent
        (result, errno, mesg) = user_service.send_email_confirm(id_user, server)
        if result:
            # Commit the database record
            db.session.commit()
            logging.info('User-controller: register success: %s', id_user)

            # Create user
            return {'success': True, 'user': id_user}
        else:
            logging.error("Unable to register user. Error %s: %s", errno, mesg)
            return {'success': False, 'user': 0}


class GetUserById(Resource):

    @staticmethod
    def get(id_user):
        # Parse numbers
        try:
            id_user = int(id_user)

        except ValueError:
            return Failures.not_a_number('idUser', id_user)

        # Validate user exists, is validated and is not blocked
        user = user_service.get_user(id_user)

        if user is None:
            return Failures.unknown_user_id(id_user)

        logging.info('User-controller: getUserById: success: %s (%s)', id_user, user.screen_name)

        return {'success': True, 'user': {
            'id': user.id,
            'email': user.email,
            'locale': user.locale,
            'screenname': user.screen_name,
            'authentication-source': user.auth_source,
            'bdmonth': user.birth_month,
            'bdyear': user.birth_year,
            'parent-email': user.parent_email,
            'parent-email-source': user.parent_email_source
        }}


class GetUserByEmail(Resource):

    @staticmethod
    def get(email):
        # TODO: Validate the format of the email address before attempting database IO

        # Validate user exists, is validated and is not blocked
        user = user_service.get_user_by_email(email)

        if user is None:
            return Failures.unknown_user_email(email)

        logging.info('User-controller: getUserByEmail: success: %s (%s)', email, user.screen_name)

        return {'success': True, 'user': {
            'id': user.id,
            'email': user.email,
            'locale': user.locale,
            'screenname': user.screen_name,
            'authentication-source': user.auth_source,
            'bdmonth': user.birth_month,
            'bdyear': user.birth_year,
            'parent-email': user.parent_email,
            'parent-email-source': user.parent_email_source
        }}


class GetUserByScreenname(Resource):

    @staticmethod
    def get(screen_name):
        # Validate user exists, is validated and is not blocked
        user = user_service.get_user_by_screen_name(screen_name)

        if user is None:
            return Failures.unknown_user_screen_name(screen_name)

        logging.info('User-controller: getUserByScreenname: success: %s (%s)', screen_name, user.screen_name)

        return {'success': True, 'user': {
            'id': user.id,
            'email': user.email,
            'locale': user.locale,
            'screenname': user.screen_name,
            'authentication-source': user.auth_source,
            'bdmonth': user.birth_month,
            'bdyear': user.birth_year,
            'parent-email': user.parent_email,
            'parent-email-source': user.parent_email_source
        }}


class DoInfoChange(Resource):
    """
        Update the screen name in the user profile.
    """

    @staticmethod
    def post(id_user):
        screen_name = request.form.get('screenname')

        # Validate required fields
        validation = Validation()
        validation.add_required_field('id-user', id_user)
        validation.add_required_field('screenname', screen_name)
        if not validation.is_valid():
            return validation.get_validation_response()

        # Validate the id parameter as an integer
        try:
            id_user = int(id_user)

        except ValueError:
            return Failures.not_a_number('idUser', id_user)

        # Validate user exists, is validated and is not blocked
        user = user_service.get_user(id_user)

        if user is None:
            return Failures.unknown_user_id(id_user)

        # Attempt to retrieve the proposed screen name to ensure that it is available
        user_by_email = user_service.get_user_by_screen_name(screen_name)

        if user_by_email is not None:
            if user.id != user_by_email.id:
                return Failures.screen_name_already_in_use(screen_name)

        # The screen name is available, Assign it to the user profile
        user.screen_name = screen_name
        db.session.commit()

        logging.info('User-controller: doInfoChange: success: %s (%s)', id_user, user.screen_name)

        return {'success': True, 'user': {
            'id': user.id,
            'email': user.email,
            'locale': user.locale,
            'screenname': user.screen_name,
            'authentication-source': user.auth_source,
            'bdmonth': user.birth_month,
            'bdyear': user.birth_year,
            'parent-email': user.parent_email,
            'parent-email-source': user.parent_email_source
        }}


class DoLocaleChange(Resource):

    @staticmethod
    def post(id_user):
        locale = request.form.get('locale')

        # Validate required fields
        validation = Validation()
        validation.add_required_field('id-user', id_user)
        validation.add_required_field('locale', locale)
        if not validation.is_valid():
            return validation.get_validation_response()

        # Parse numbers
        try:
            id_user = int(id_user)
        except ValueError:
            return Failures.not_a_number('idUser', id_user)

        # Validate user exists, is validated and is not blocked
        user = user_service.get_user(id_user)
        if user is None:
            return Failures.unknown_user_id(id_user)

        user.locale = locale
        db.session.commit()

        logging.info('User-controller: doLocaleChange: success: %s (%s)', id_user, user.screen_name)

        return {'success': True, 'user': {
            'id': user.id,
            'email': user.email,
            'locale': user.locale,
            'screenname': user.screen_name,
            'authentication-source': user.auth_source,
            'bdmonth': user.birth_month,
            'bdyear': user.birth_year,
            'parent-email': user.parent_email,
            'parent-email-source': user.parent_email_source
        }}


# Supported endpoints
# Note: The url_prefix is '/user'. All user endpoints are in the form
# of host:port/user/_service_
#
# Register a new user account
api.add_resource(Register, '/register')

# Retrieve details about an existing user account
api.add_resource(GetUserById, '/id/<int:id_user>')
api.add_resource(GetUserByEmail, '/email/<string:email>')
api.add_resource(GetUserByScreenname, '/screenname/<string:screen_name>')

# Update a user screen name
api.add_resource(DoInfoChange, '/info/<int:id_user>')

# Update the local defined in the user account
api.add_resource(DoLocaleChange, '/locale/<int:id_user>')
