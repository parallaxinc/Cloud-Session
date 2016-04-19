# Import the database object from the main app module
import logging
import uuid
import datetime

import Failures
from app import db
from app.User import services as user_services
from app.RateLimiting import services as rate_limiting_services

from flask_restful import Resource, Api
from flask import request, Blueprint

from Validation import Validation


authenticate_app = Blueprint('authenticate', __name__, url_prefix='/authenticate')
api = Api(authenticate_app)


class AuthenticateLocalUser(Resource):

    def post(self):
        # Get values
        server = request.headers.get('server')
        email = request.form.get('email')
        password = request.form.get('password')
        #browser = request.form.get('browser')
        #ip_address = request.form.get('ipAddress')

        # Validate required fields
        validation = Validation()
        validation.add_required_field('server', server)
        validation.add_required_field('email', email)
        validation.add_required_field('password', password)
        #validation.add_required_field('browser', browser)
        #validation.add_required_field('ipAddress', ip_address)
        if not validation.is_valid():
            return validation.get_validation_response()

        # Validate user exists, is validated and is not blocked
        user = user_services.get_user_by_email(email)
        if user is None:
            return Failures.unknown_user_email(email)
        if not user.confirmed:
            return Failures.email_not_confirmed()
        if user.blocked:
            return Failures.user_blocked()

        if not rate_limiting_services.has_sufficient_tokens(user.id, 'failed-password', 1):
            return Failures.rate_exceeded()

        if not user_services.check_password(user.id, password):
            rate_limiting_services.consume_tokens(user.id, 'failed-password', 1)
            db.session.commit()
            return Failures.wrong_password()

        db.session.commit()

        logging.info('Authenticate-controller: Authenticate: success: %s', user.id)

        return {'success': True, 'user': {
            'id': user.id,
            'email': user.email,
            'locale': user.locale,
            'screenname': user.screen_name
        }}

api.add_resource(AuthenticateLocalUser, '/local')
