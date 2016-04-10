from app import mail, app, db

from os.path import expanduser, isfile

from flask.ext.mail import Message

import pystache


def send_email_template_for_user(id_user, template, server, **kwargs):
    from app.User.services import get_user

    params = {}
    for key, value in kwargs.items():
        params[key] = value

    user = get_user(id_user)
    if user is None:
        return False

    params['screenname'] = user.screen_name

    send_email_template_to_address(user.email, template, server, user.locale, params)


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
        subject=subject,
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
    template_file = expanduser("~/templates/%s/%s/%s/%s.mustache" % (locale, template, server, part))
    print('Looking for template file: %s' % template_file)
    if isfile(template_file):
        renderer = pystache.Renderer()
        rendered = renderer.render_path(template_file, params)
        #print(rendered)
        return rendered
    else:
        if none_if_missing:
            return None
        else:
            return 'Template missing'
