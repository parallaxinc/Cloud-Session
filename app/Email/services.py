from app import mail, app, db

from flask.ext.mail import Message


def send_email_template_for_user(id_user, template, server, **kwargs):
    from app.User.services import get_user

    user = get_user(id_user)
    if user is None:
        return False

    # Read templates


def send_email(recipient, subject, email_text, rich_email_text=None):
    pass

    # msg = Message(recipients=[email], body='Test', subject='Subject', sender=app.config['DEFAULT_MAIL_SENDER'])
    # mail.send(msg)
