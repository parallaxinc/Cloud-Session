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
