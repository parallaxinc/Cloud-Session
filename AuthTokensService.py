from flask_restful import Resource
from flask import request

from Validation import Validation
#from models import AuthenticationToken


class AuthTokensRequest(Resource):

    def post(self):
        server = request.headers.get('server')
        id_user = request.form.get('idUser')
        browser = request.form.get('browser')
        ip_address = request.form.get('ipAddress')

        validation = Validation()
        validation.add_required_field('server', server)
        validation.add_required_field('idUser', id_user)
        validation.add_required_field('browser', browser)
        validation.add_required_field('ipAddress', ip_address)
        if not validation.is_valid():
            return validation.get_validation_response(), 400

        #authentication_token = AuthenticationToken()
        #authentication_token.id_user = id_user
        #authentication_token.browser = browser
        #authenticaiton_token.validity =

        return {'status': 'success', 'server': server}
