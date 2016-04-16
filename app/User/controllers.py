import Failures
from app import db, mail, app


from flask_restful import Resource, Api
from flask import request, Blueprint

from Validation import Validation

from app.User import services as user_service
from models import User

user_app = Blueprint('user', __name__, url_prefix='/user')
api = Api(user_app)


class Register(Resource):

    def post(self):
        # Get values
        server = request.headers.get('server')
        email = request.form.get('email')
        password = request.form.get('password')
        password_confirm = request.form.get('password-confirm')
        locale = request.form.get('locale')
        screen_name = request.form.get('screenname')

        # Validate required fields
        validation = Validation()
        validation.add_required_field('server', server)
        validation.add_required_field('email', email)
        validation.add_required_field('password', password)
        validation.add_required_field('password-confirm', password_confirm)
        validation.add_required_field('locale', locale)
        validation.add_required_field('screenname', screen_name)
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

        id_user = user_service.create_local_user(server, email, password, locale, screen_name)
        user_service.send_email_confirm(id_user, server)

        db.session.commit()

        # Create user
        return {'success': True, 'user': id_user}


class GetUserById(Resource):

    def get(self, id_user):
        # Parse numbers
        try:
            id_user = int(id_user)
        except:
            return Failures.not_a_number('idUser', id_user)

        # Validate user exists, is validated and is not blocked
        user = user_service.get_user(id_user)
        if user is None:
            return Failures.unknown_user_id(id_user)

        return {'success': True, 'user': {
            'id': user.id,
            'email': user.email,
            'locale': user.locale,
            'screenname': user.screen_name
        }}


class GetUserByEmail(Resource):

    def get(self, email):
        # Validate user exists, is validated and is not blocked
        user = user_service.get_user_by_email(email)
        if user is None:
            return Failures.unknown_user_email(email)

        return {'success': True, 'user': {
            'id': user.id,
            'email': user.email,
            'locale': user.locale,
            'screenname': user.screen_name
        }}


class DoInfoChange(Resource):

    def post(self, id_user):
        screen_name = request.form.get('screenname')
        # Validate required fields
        validation = Validation()
        validation.add_required_field('id-user', id_user)
        validation.add_required_field('screenname', screen_name)
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

        user_by_email = user_service.get_user_by_screen_name(screen_name)
        if user_by_email is not None:
            if user.id != user_by_email.id:
                return Failures.screen_name_already_in_use(screen_name)

        user.screen_name = screen_name
        db.session.commit()

        return {'success': True, 'user': {
            'id': user.id,
            'email': user.email,
            'locale': user.locale,
            'screenname': user.screen_name
        }}


class DoLocaleChange(Resource):

    def post(self, id_user):
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
        except:
            return Failures.not_a_number('idUser', id_user)

        # Validate user exists, is validated and is not blocked
        user = user_service.get_user(id_user)
        if user is None:
            return Failures.unknown_user_id(id_user)

        user.locale = locale
        db.session.commit()

        return {'success': True, 'user': {
            'id': user.id,
            'email': user.email,
            'locale': user.locale,
            'screenname': user.screen_name
        }}


api.add_resource(Register, '/register')
api.add_resource(GetUserById, '/id/<int:id_user>')
api.add_resource(GetUserByEmail, '/email/<string:email>')
api.add_resource(DoInfoChange, '/info/<int:id_user>')
api.add_resource(DoLocaleChange, '/locale/<int:id_user>')