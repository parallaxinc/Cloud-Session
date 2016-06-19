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
            # New user
            return Failures.unknown_user_email(email)

        logging.info('OAuth-controller: Validate: success: %s', user.id)

        return {'success': True}


api.add_resource(ValidateUser, '/validate')
