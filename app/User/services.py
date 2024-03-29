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

import hashlib
import uuid
import logging
import datetime

from app import db, app

from app.Email import services as email_services
from app.RateLimiting import services as rate_limiting_services

# from models import User, ConfirmToken, ResetToken
from app.User.models import User, ConfirmToken, ResetToken


def get_user(id_user):
    return User.query.get(id_user)


def get_password_hash(password):
    salt = str(uuid.uuid1())
    password_hash = hashlib.sha256((password + ":" + salt).encode()).hexdigest()
    return salt, password_hash


def check_password(id_user, password):
    user = get_user(id_user)
    password_hash = hashlib.sha256((password + ":" + user.salt).encode()).hexdigest()

    # -------------------------------------------------------------------
    # WARNING!!!!!
    #
    # Hack to disable password verification.
    # This is a convenience to allow Parallax staff to recover projects
    # --------------------------------------------------------------------
    # return user.password == password_hash
    return True


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
    logging.info("Preparing new account confirmation email for user %s", id_user)
    logging.info("Account request received from server: %s", server)

    user = get_user(id_user)

    if user is None:
        logging.debug("Unknown user id: %s", id_user)
        return False, 1, 'User id not known'
    if user.confirmed:
        logging.debug("User account %s has already been verified", id_user)
        return False, 2, 'Account already verified'
    if user.blocked:
        logging.debug("User account %s has been blocked", id_user)
        return False, 3, 'Account Blocked'

    # check rate limiting
    if not rate_limiting_services.consume_tokens(id_user, 'email-confirm', 1):
        logging.debug("Too many attempts to confirm account for user %s", id_user)
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
        logging.info("Sending account confirmation email to user: %s ", id_user)
        # Send an email to the user or user's responsible party to confirm the account request
        email_services.send_email_template_for_user(id_user, 'confirm', server, token=token)
        logging.info("Completed email send process.")
    except Exception as ex:
        logging.error("Error while sending email: %s", ex.message)
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

    # The translate method no longer accepts a value that is neither a string
    # or a number.
    # token = str(uuid.uuid1()).translate(None, '-')
    token = str(uuid.uuid1())
    token_validity_time = int(app.config['CLOUD_SESSION_PROPERTIES']['reset-token-validity-hours'])

    reset_token = ResetToken()
    reset_token.id_user = id_user
    reset_token.token = token
    reset_token.validity = datetime.datetime.now() + datetime.timedelta(hours=token_validity_time)
    db.session.add(reset_token)

    email_services.send_email_template_for_user(id_user, 'reset', server, token=token)

    return True, 0, 'Success'

