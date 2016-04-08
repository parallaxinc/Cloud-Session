from validate_email import validate_email


class Validation:

    required_but_missing = []
    invalid_email = []

    def __init__(self):
        pass

    def add_required_field(self, field, value):
        if value is None:
            self.required_but_missing.append(field)

    def check_email(self, field, email):
        if email is not None:
            if not validate_email(email):
                self.invalid_email.append(field)

    def is_valid(self):
        if len(self.required_but_missing) > 0:
            return False
        if len(self.invalid_email) > 0:
            return False
        return True

    def get_validation_response(self):
        return {
            'success': False,
            'message': 'Validation error',
            'code': 300,
            'missing-fields': self.required_but_missing,
            'invalid-email-fields': self.invalid_email
        }
