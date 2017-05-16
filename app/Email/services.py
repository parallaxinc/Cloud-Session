from app import mail, app
from os.path import expanduser, isfile
from flask.ext.mail import Message
from app.User.coppa import Coppa, SponsorType

import pystache
import logging


"""
TODO: System documentation goes here
"""


def send_email_template_for_user(id_user, template, server, **kwargs):
    from app.User.services import get_user

    # Get a copy of the user record
    logging.info("Checking for a valid user record for user ID: %s", id_user)
    user = get_user(id_user)

    if user is None:
        logging.error("Cannot send email: Invalid user record")
        return False
    else:
        logging.info("Email template received user: %s", user.id)

    logging.info("Sending email to user: %s using template (%s)", user.id, template)

    params = {}
    for key, value in kwargs.items():
        logging.debug("Logging parameter %s = %s", key, value)
        params[key] = value


    # The elements in the params array represent the data elements that are
    # available to the email templates.
    params['screenname'] = user.screen_name
    params['email'] = user.email
    params['registrant-email'] = user.email
    params['sponsoremail'] = user.parent_email

    #Default the recipient email address
    user_email = user.email
    coppa = Coppa()

    # Send email to parent if user is under 13 years old
    if template == 'confirm' and coppa.is_coppa_covered(user.birth_month, user.birth_year):
        # Send email only to the sponsor address
        user_email = user.parent_email
        logging.info("COPPA account has a sponsor type of %s", user.parent_email_source)

        if user.parent_email_source == SponsorType.TEACHER:
            # Teacher handles the account confirmation
            send_email_template_to_address(user_email, 'confirm-teacher', server, user.locale, params)
        elif user.parent_email_source == SponsorType.PARENT or\
                        user.parent_email_source == SponsorType.GUARDIAN:
            # Parent handles the account confirmation
            send_email_template_to_address(user_email, 'confirm-parent', server, user.locale, params)
        else:
            logging.info("COPPA account %s has invalid sponsor type [%s]", user.id, user.parent_email_source)

        return
    else:
        # Registration not subject to COPPA regulations
        send_email_template_to_address(user_email, template, server, user.locale, params)

    return


def send_email_template_to_address(recipient, template, server, locale, params=None, **kwargs):
    params = params or {}

    # Add any supplied arguments to the parameter dictionary
    for key, value in kwargs.items():
        params[key] = value

    params['email'] = recipient
    params['locale'] = locale

    # Read templates
    (subject, plain, rich) = _read_templates(template, server, locale, params)

    logging.info("Sending email to %s", recipient)
    send_email(recipient, subject, plain, rich)


def send_email(recipient, subject, email_text, rich_email_text=None):

    msg = Message(
        recipients=[recipient],
        subject=subject.rstrip(),
        body=email_text,
        html=rich_email_text,
        sender=app.config['DEFAULT_MAIL_SENDER']
    )
    mail.send(msg)


def _read_templates(template, server, locale, params):
    header = _read_template(template, server, locale, 'header', params)
    plain = _read_template(template, server, locale, 'plain', params)
    rich = _read_template(template, server, locale, 'rich', params, True)

    return header, plain, rich


def _read_template(template, server, locale, part, params, none_if_missing=False):
    """
    Render a mustache template.

    :param template: Base template name
    :param server: Host server
    :param locale: Language designator
    :param part: Generic message type descriptor
    :param params: Text string to replace tags embedded within the template
    :param none_if_missing: Return 'none' if the requested template is not found

    :return: Upon success, return a Renderer object. Return none or a general
             error message if the none_is_missing flag is false
    """
    template_file = expanduser("~/templates/%s/%s/%s/%s.mustache" % (locale, template, server, part))
    if isfile(template_file):
        logging.debug('Looking for template file: %s', template_file)
        renderer = pystache.Renderer()
        rendered = renderer.render_path(template_file, params)
        return rendered
    else:
        logging.warn('Looking for template file: %s, but the file is missing', template_file)
        if none_if_missing:
            return None
        else:
            return 'Template missing'
