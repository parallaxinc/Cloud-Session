from flask_restful import Resource
from flask import request


class AuthTokensRequest(Resource):

    def post(self):
        server = request.headers.get('server')
        return {'status': 'success', 'server': server}
