from validate_email import validate_email

import logging


class Validation:

    def __init__(self):
        self.required_but_missing = []
        self.invalid_email = []

    def add_required_field(self, field, value):
        if value is None:
            logging.debug('Validator: required field %s is missing', field)
            self.required_but_missing.append(field)

    def check_email(self, field, email):
        if email is not None:
            if not validate_email(email):
                logging.debug('Validator: email field %s is invalid: %s', field, email)
                self.invalid_email.append(field)

    def is_valid(self):
        if len(self.required_but_missing) > 0:
            return False
        if len(self.invalid_email) > 0:
            return False
        return True

    def get_validation_response(self):
        logging.debug('Validator: creating rest response')
        return {
            'success': False,
            'message': 'Validation error',
            'code': 300,
            'missing-fields': self.required_but_missing,
            'invalid-email-fields': self.invalid_email
        }, 400
