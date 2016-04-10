import Failures
from app import db, app


from flask_restful import Resource, Api
from flask import request, Blueprint

from Validation import Validation

from app.User import services as user_service
from app.User.models import ConfirmToken

local_user_app = Blueprint('local_user', __name__, url_prefix='/local')
api = Api(local_user_app)


class DoConfirm(Resource):

    def post(self):
        # Get values
        email = request.form.get('email')
        token = request.form.get('token')

        # Validate required fields
        validation = Validation()
        validation.add_required_field('email', email)
        validation.add_required_field('token', token)
        validation.check_email('email', email)
        if not validation.is_valid():
            return validation.get_validation_response()

        # Validate user exits
        user = user_service.get_user_by_email(email)
        if user is None:
            return Failures.unknown_user_email(email)

        confirm_token = ConfirmToken.query.filter_by(token=token).first()
        if confirm_token is None:
            # Unkown token
            return {'success': False, 'code': 510}
        if confirm_token.id_user != user.id:
            # Token is not for this user
            return {'success': False, 'code': 510}

        user.confirmed = True

        db.session.delete(confirm_token)
        db.session.commit()

        return {'success': True}


class RequestConfirm(Resource):

    def get(self, email):
        # Get values
        server = request.headers.get('server')

        # Validate required fields
        validation = Validation()
        validation.add_required_field('email', email)
        validation.add_required_field('server', server)
        validation.check_email('email', email)
        if not validation.is_valid():
            return validation.get_validation_response()

        # Validate user exits
        user = user_service.get_user_by_email(email)
        if user is None:
            return Failures.unknown_user_email(email)

        success, code, message = user_service.send_email_confirm(user.id, server)

        db.session.commit()

        if success:
            return {'success': True}
        else:
            return {
                'success': False,
                'message': message,
                'code': 520
            }


api.add_resource(DoConfirm, '/confirm')
api.add_resource(RequestConfirm, '/confirm/<string:email>')
