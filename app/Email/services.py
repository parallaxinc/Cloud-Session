from app import mail, app
from os.path import expanduser, isfile
from flask.ext.mail import Message
from app.User.services import SponsorType

import pystache
import logging


def send_email_template_for_user(id_user, template, server, **kwargs):
    from app.User.services import get_user, is_coppa_covered

    logging.info("Sending email to user: %s (%s)", id_user, template)

    params = {}
    for key, value in kwargs.items():
        params[key] = value

    user = get_user(id_user)
    if user is None:
        return False

    params['screenname'] = user.screen_name

    # Send email to parent if user is under 13 years old
    if template == 'confirm':
        if is_coppa_covered(user.birth_month, user.birth_year):
            user_email = user.parent_email

            if user.parent_email_source == SponsorType.TEACHER:
                # Teacher handles the account confirmation
                send_email_template_to_address(user_email, 'confim_teacher', server, user.locale, params)
            elif user.parent_email_source == SponsorType.PARENT or user.parent_email_source == SponsorType.GUARDIAN:
                # Parent handles the account confirmation
                send_email_template_to_address(user_email, 'confirm_parent', server, user.locale, params)
            else:
                logging.info("COPPA account %s has invalid sponsor type [%s]", id_user, user.parent_email_source)
    else:
        user_email = user.email

    send_email_template_to_address(user_email, template, server, user.locale, params)


def send_email_template_to_address(recipient, template, server, locale, params=None, **kwargs):
    # Read templates
    params = params or {}
    for key, value in kwargs.items():
        params[key] = value
    params['email'] = recipient
    params['locale'] = locale

    (subject, plain, rich) = _read_templates(template, server, locale, params)

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
