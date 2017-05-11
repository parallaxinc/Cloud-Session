import hashlib
import uuid
import logging
import datetime

from app import db, app

from app.Email import services as email_services
from app.RateLimiting import services as rate_limiting_services

from models import User, ConfirmToken, ResetToken


# Implementing an enum-like structure for the user sponsor email type
class SponsorType:
    INDIVIDUAL = 0
    PARENT = 1
    GUARDIAN = 2
    TEACHER = 3


def get_password_hash(password):
    salt = str(uuid.uuid1())
    password_hash = hashlib.sha256("%s:%s" % (password, salt)).hexdigest()
    return salt, password_hash


def check_password(id_user, password):
    user = get_user(id_user)
    password_hash = hashlib.sha256("%s:%s" % (password, user.salt)).hexdigest()
    return user.password == password_hash


def get_user(id_user):
    return User.query.get(id_user)


def get_user_by_email(email):
    return User.query.filter_by(email=email).first()


def get_user_by_screen_name(screen_name):
    return User.query.filter_by(screen_name=screen_name).first()


def check_password_complexity(password):
    return 8 <= len(password) < 200


def create_local_user(
        server, email, password, locale, screen_name,
        birth_month, birth_year, parent_email, parent_email_source):

    salt, password_hash = get_password_hash(password)

    # Save user
    user = User()
    user.email = email
    user.locale = locale
    user.screen_name = screen_name
    user.auth_source = 'local'
    user.password = password_hash
    user.salt = salt

    #COPPA support
    user.birth_month = birth_month
    user.birth_year = birth_year
    user.parent_email = parent_email
    user.parent_email_source = parent_email_source

    db.session.add(user)
    db.session.flush()
    db.session.refresh(user)

    return user.id


def create_oauth_user(
        server, email, source, locale, screen_name,
        birth_month, birth_year, parent_email, parent_email_source):

    # Save user
    user = User()
    user.email = email
    user.locale = locale
    user.screen_name = screen_name
    user.auth_source = source
    user.confirmed = True
    user.blocked = False

    # COPPA support
    user.birth_month = birth_month
    user.birth_year = birth_year
    user.parent_email = parent_email
    user.parent_email_source = parent_email_source

    # Add the user record
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

    # check rate limiting
    if not rate_limiting_services.consume_tokens(id_user, 'email-confirm', 1):
        return False, 10, 'Rate limiter exceeded'

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

    try:
        email_services.send_email_template_for_user(id_user, 'confirm', server, token=token)
    except Exception as ex:
        print("Exception {0}", ex.args)
        logging.error("Unable to send email. Message is: %s", ex.message)
        return False, 99, 'Unable to contact SMTP server'

    return True, 0, 'Success'


def send_password_reset(id_user, server):
    user = get_user(id_user)
    if user is None:
        return False, 1, 'User id not known'
    if user.blocked:
        return False, 3, 'Account Blocked'

    # check rate limiting
    if not rate_limiting_services.consume_tokens(id_user, 'password-reset', 1):
        return False, 10, 'Rate limiter exceeded'

    # Delete token if any exists
    existing_token = ResetToken.query.filter_by(id_user=id_user).first()
    if existing_token is not None:
        db.session.delete(existing_token)
        db.session.flush()

    token = str(uuid.uuid1()).translate(None, '-')
    token_validity_time = int(app.config['CLOUD_SESSION_PROPERTIES']['reset-token-validity-hours'])

    reset_token = ResetToken()
    reset_token.id_user = id_user
    reset_token.token = token
    reset_token.validity = datetime.datetime.now() + datetime.timedelta(hours=token_validity_time)
    db.session.add(reset_token)

    email_services.send_email_template_for_user(id_user, 'reset', server, token=token)

    return True, 0, 'Success'

# Return true if the date is less than 13 years
def is_coppa_covered(month,year):
    # This is the number of months a typical thirteen years old has been on planet Earth.
    cap = 156

    # This is the actual number of months a typical user has been on the same planet.
    user_age = (year * 12) + month

    # Current year and month
    current_month = datetime.date.today().month
    current_year = datetime.date.today().year

    # This represents the number of months since the inception of AD
    # Unless you want to count that first year as part of BC.
    current_cap = (current_year * 12) + current_month

    if current_cap - user_age > cap:
        return False
    else:
        return True
