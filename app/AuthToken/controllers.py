# Import the database object from the main app module
import uuid
import datetime

import Failures
from app import db
from app.User import services as user_service

from flask_restful import Resource, Api
from flask import request, Blueprint

from Validation import Validation

from models import AuthenticationToken

auth_token_app = Blueprint('authtoken', __name__, url_prefix='/authtoken')
api = Api(auth_token_app)


class AuthTokensRequest(Resource):

    def post(self):
        # Get values
        server = request.headers.get('server')
        id_user = request.form.get('idUser')
        browser = request.form.get('browser')
        ip_address = request.form.get('ipAddress')

        # Validate required fields
        validation = Validation()
        validation.add_required_field('server', server)
        validation.add_required_field('idUser', id_user)
        validation.add_required_field('browser', browser)
        validation.add_required_field('ipAddress', ip_address)
        if not validation.is_valid():
            return validation.get_validation_response()

        # Parse numbers
        try:
            id_user = int(id_user)
        except:
            return Failures.not_a_number('idUser', id_user)

        # Validate user exists, is validated and is not blocked
        user = user_service.get_user(id_user)
        if user is None:
            return Failures.unknown_user_id(id_user)
        if not user.confirmed:
            return Failures.email_not_confirmed()
        if user.blocked:
            return Failures.user_blocked()

        # Generate token
        token = str(uuid.uuid1())

        # Save token and browser information
        authentication_token = AuthenticationToken()
        authentication_token.id_user = id_user
        authentication_token.browser = browser
        authentication_token.server = server
        authentication_token.ip_address = ip_address
        authentication_token.validity = datetime.datetime.now() + datetime.timedelta(minutes=120)
        authentication_token.token = token
        db.session.add(authentication_token)
        db.session.commit()

        return {'success': True, 'token': token}

api.add_resource(AuthTokensRequest, '/request')
