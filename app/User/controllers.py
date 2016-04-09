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


api.add_resource(Register, '/register')
