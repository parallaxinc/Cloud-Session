import logging


def unknown_user_id(id_user):
    logging.debug('Failures: Unknown user id: %s', id_user)
    return {
               'success': False,
               'message': 'Unknown user',
               'code': 400,
               'data': id_user
           }, 500


def unknown_user_email(email):
    logging.debug('Failures: Unknown user email: %s', email)
    return {
               'success': False,
               'message': 'Unknown user',
               'code': 400,
               'data': email
           }, 500


def unknown_user_screen_name(screen_name):
    logging.debug('Failures: Unknown user by screen name: %s', screen_name)
    return {
               'success': False,
               'message': 'Unknown user screen name',
               'code': 400,
               'data': screen_name
           }, 500


def email_already_in_use(email):
    logging.debug('Failures: Email already in use: %s', email)
    return {
               'success': False,
               'message': 'Email already in use',
               'code': 450,
               'data': email
           }, 500


def email_not_confirmed(email):
    logging.debug('Failures: Email %s not confirmed', email)
    return {
               'success': False,
               'message': 'Email not confirmed',
               'code': 430
           }, 401


def user_blocked(email):
    logging.debug('Failures: User %s blocked', email)
    return {
               'success': False,
               'message': 'User is blocked',
               'code': 420
           }, 401


def not_a_number(field, value):
    logging.error('Failures: Not a valid number: %s -> %s', field, value)
    return {
               'success': False,
               'message': 'Not a valid number',
               'code': 310,
               'field': field,
               'value': value
           }, 400


def passwords_do_not_match():
    logging.debug('Failures: Passwords do not match')
    return {
               'success': False,
               'message': "Password confirm doesn't match",
               'code': 460
           }, 500


def password_complexity():
    logging.debug('Failures: Password is not complex enough')
    return {
               'success': False,
               'message': "Password is not complex enough",
               'code': 490
           }, 500


def screen_name_already_in_use(screen_name):
    logging.debug('Failures: Screen name already in use: %s', screen_name)
    return {
               'success': False,
               'message': "Screenname already in use",
               'data': screen_name,
               'code': 500
           }, 500


def rate_exceeded(time):
    """
      Service requested to frequently.

      time - string representing the date and time the service will be available again
    """
    logging.debug('Failures: Rate exceeded')
    return {
               'success': False,
               'message': 'Insufficient bucket tokens',
               'data': time,
               'code': 470
           }, 500


def wrong_password(email):
    logging.debug('Failures: Wrong password for %s', email)
    return {
               'success': False,
               'message': 'Wrong password',
               'code': 410
           }, 401


def unknown_bucket_type(bucket_type):
    logging.debug('Failures: Unknown bucket type: %s', bucket_type)
    return {
               'success': False,
               'message': 'Unknown bucket type',
               'code': 180,
               'data': bucket_type
           }, 500


def wrong_auth_source(auth_source):
    logging.debug('Failures: Wrong auth source: %s', auth_source)
    return {
               'success': False,
               'message': 'Wrong auth source',
               'code': 480,
               'data': auth_source
           }, 500
