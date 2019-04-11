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

# Import the database object from the main app module
import logging
import uuid
import datetime
import Failures

from app import db
from app.User import services as user_service

from flask_restful import Resource, Api
from flask import request, Blueprint

from Validation import Validation

from app.AuthToken.models import AuthenticationToken

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
        except ValueError:
            return Failures.not_a_number('idUser', id_user)

        # Validate user exists, is validated and is not blocked
        user = user_service.get_user(id_user)
        if user is None:
            return Failures.unknown_user_id(id_user)
        if not user.confirmed:
            return Failures.email_not_confirmed()
        if user.blocked:
            return Failures.user_blocked()

        # Delete expired tokens
        AuthenticationToken.query.filter(AuthenticationToken.validity < datetime.datetime.now()).delete()
        db.session.flush()

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

        logging.info('AuthToken-controller: Request auth token: success: %s -> %s*****', user.id, token[0:6])

        return {'success': True, 'token': token}


class GetAuthTokens(Resource):

    def post(self, id_user):
        # Get values
        server = request.headers.get('server')
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

        authentication_token_models = AuthenticationToken.query.filter_by(
            id_user=id_user,
   #         browser=browser,
   #         server=server,
   #         ip_address=ip_address
        ).all()

        authentication_tokens = []
        for authentication_token_model in authentication_token_models:
            authentication_tokens.append(authentication_token_model.token)

        logging.info('AuthToken-controller: Get auth tokens: success: %s -> %s tokens', id_user,
                     len(authentication_tokens))

        return authentication_tokens

api.add_resource(AuthTokensRequest, '/request')
api.add_resource(GetAuthTokens, '/tokens/<int:id_user>')
