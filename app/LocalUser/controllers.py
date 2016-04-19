import datetime
import logging

import Failures
from app import db, app


from flask_restful import Resource, Api
from flask import request, Blueprint

from Validation import Validation

from app.User import services as user_service
from app.User.models import ConfirmToken, ResetToken

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

        # Delete expired tokens
        ConfirmToken.query.filter(ConfirmToken.validity < datetime.datetime.now()).delete()
        db.session.flush()

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

        logging.info('LocalUser-controller: DoConfirm: success: %s', user.id)

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
            logging.info('LocalUser-controller: RequestConfirm: success: %s', user.id)

            return {'success': True}
        else:
            if code == 10:
                return Failures.rate_exceeded()
            return {
                'success': False,
                'message': message,
                'code': 520
            }


class PasswordReset(Resource):

    def post(self, email):
        # Get values
        token = request.form.get('token')
        password = request.form.get('password')
        password_confirm = request.form.get('password-confirm')

        # Validate required fields
        validation = Validation()
        validation.add_required_field('email', email)
        validation.add_required_field('token', token)
        validation.add_required_field('password', password)
        validation.add_required_field('password-confirm', password_confirm)
        validation.check_email('email', email)
        if not validation.is_valid():
            return validation.get_validation_response()

        # Validate user exits
        user = user_service.get_user_by_email(email)
        if user is None:
            return Failures.unknown_user_email(email)

        # Validate password strength and confirm
        if password != password_confirm:
            return Failures.passwords_do_not_match()
        if not user_service.check_password_complexity(password):
            return Failures.password_complexity()

        # Delete expired tokens
        ResetToken.query.filter(ResetToken.validity < datetime.datetime.now()).delete()
        db.session.flush()

        reset_token = ResetToken.query.filter_by(token=token).first()
        if reset_token is None:
            # Unkown token
            return {'success': False, 'code': 510}
        if reset_token.id_user != user.id:
            # Token is not for this user
            return {'success': False, 'code': 510}

        salt, password_hash = user_service.get_password_hash(password)
        user.password = password_hash
        user.salt = salt

        db.session.delete(reset_token)
        db.session.commit()

        logging.info('LocalUser-controller: DoPasswordReset: success: %s', user.id)

        return {'success': True}

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

        success, code, message = user_service.send_password_reset(user.id, server)

        db.session.commit()

        if success:
            logging.info('LocalUser-controller: RequestPasswordReset: success: %s', user.id)
            return {'success': True}
        else:
            if code == 10:
                return Failures.rate_exceeded()
            return {
                'success': False,
                'message': message,
                'code': 520
            }


class PasswordChange(Resource):

    def post(self, id_user):
        # Get values
        old_password = request.form.get('old-password')
        password = request.form.get('password')
        password_confirm = request.form.get('password-confirm')

        # Validate required fields
        validation = Validation()
        validation.add_required_field('id_user', id_user)
        validation.add_required_field('old-password', old_password)
        validation.add_required_field('password', password)
        validation.add_required_field('password_confirm', password_confirm)
        if not validation.is_valid():
            return validation.get_validation_response()

        # Validate user exits
        user = user_service.get_user(id_user)
        if user is None:
            return Failures.unknown_user_id(id_user)

        # Validate password strength and confirm
        if password != password_confirm:
            return Failures.passwords_do_not_match()
        if not user_service.check_password_complexity(password):
            return Failures.password_complexity()

        if not user_service.check_password(id_user, old_password):
            # Token is not for this user
            return {'success': False, 'code': 530}

        salt, password_hash = user_service.get_password_hash(password)
        user.password = password_hash
        user.salt = salt

        db.session.commit()

        logging.info('LocalUser-controller: PasswordChange: success: %s', user.id)

        return {'success': True}


api.add_resource(DoConfirm, '/confirm')
api.add_resource(RequestConfirm, '/confirm/<string:email>')
api.add_resource(PasswordReset, '/reset/<string:email>')
api.add_resource(PasswordChange, '/password/<int:id_user>')
