def unknown_user_id(id_user):
    return {
               'success': False,
               'message': 'Unknown user',
               'code': 400,
               'data': id_user
           }, 500


def unknown_user_email(email):
    return {
               'success': False,
               'message': 'Unknown user',
               'code': 400,
               'data': email
           }, 500


def email_already_in_use(email):
    return {
               'success': False,
               'message': 'Email already in use',
               'code': 450,
               'data': email
           }, 500


def email_not_confirmed():
    return {
               'success': False,
               'message': 'Email not confirmed',
               'code': 430
           }, 401


def user_blocked():
    return {
               'success': False,
               'message': 'User is blocked',
               'code': 420
           }, 401


def not_a_number(field, value):
    return {
               'success': False,
               'message': 'Not a valid number',
               'code': 310,
               'field': field,
               'value': value
           }, 400


def passwords_do_not_match():
    return {
               'success': False,
               'message': "Password confirm doesn't match",
               'code': 460
           }, 500


def password_complexity():
    return {
               'success': False,
               'message': "Password is not complex enough",
               'code': 490
           }, 500


def screen_name_already_in_use(screen_name):
    return {
               'success': False,
               'message': "Screenname already in use",
               'data': screen_name,
               'code': 500
           }, 500


def rate_exceeded():
    return {
               'success': False,
               'message': 'Insufficient bucket tokens',
               'code': 470
           }, 500


def wrong_password():
    return {
               'success': False,
               'message': 'Wrong password',
               'code': 410
           }, 401


def unknown_bucket_type(bucket_type):
    return {
               'success': False,
               'message': 'Unknown bucket type',
               'code': 180,
               'data': bucket_type
           }, 500
