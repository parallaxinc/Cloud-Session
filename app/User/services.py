import hashlib
import uuid

import datetime

from app import db, app

from app.Email import services as email_services

from models import User, ConfirmToken


def get_user(id_user):
    return User.query.get(id_user)


def get_user_by_email(email):
    return User.query.filter_by(email=email).first()


def get_user_by_screen_name(screen_name):
    return User.query.filter_by(screen_name=screen_name).first()


def check_password_complexity(password):
    return 8 <= len(password) < 200


def create_local_user(server, email, password, locale, screen_name):
    salt = str(uuid.uuid1())
    password_hash = hashlib.sha256("%s:%s" % (password, salt)).hexdigest()

    # Save user
    user = User()
    user.email = email
    user.locale = locale
    user.screen_name = screen_name
    user.auth_source = 'local'
    user.password = password_hash
    user.salt = salt

    db.session.add(user)
    db.session.flush()
    db.session.refresh(user)

    return user.id


def send_email_confirm(id_user, server):
    user = get_user(id_user)
    if user is None:
        return False, 1, 'User id not known'
    if user.confirmed:
        return False, 2, 'Account already verified'
    if user.blocked:
        return False, 3, 'Account Blocked'

    # TODO check rate limiting

    # Delete token if any exists
    existing_token = ConfirmToken.query.filter_by(id_user=id_user).first()
    if existing_token is not None:
        db.session.delete(existing_token)
        db.session.flush()

    token = str(uuid.uuid1()).translate(None, '-')
    token_validity_time = int(app.config['CLOUD_SESSION_PROPERTIES']['confirm-token-validity-hours'])

    confirm_token = ConfirmToken()
    confirm_token.id_user = id_user
    confirm_token.token = token
    confirm_token.validity = datetime.datetime.now() + datetime.timedelta(hours=token_validity_time)
    db.session.add(confirm_token)

    email_services.send_email_template_for_user(id_user, 'confirm', server, token=token)

    return True, 0, 'Success'
