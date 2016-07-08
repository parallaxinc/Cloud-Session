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
