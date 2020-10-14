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

from app import app
from os.path import isfile
from app.User.coppa import Coppa, SponsorType

import pystache
import logging

import smtplib
import email.utils
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText


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

    logging.info("Sending email to user: %s using template: '%s'.", user.email, template)

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
    params['blocklyprop-host'] = app.config['CLOUD_SESSION_PROPERTIES']['response.host']

    # Default the recipient email address
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
    elif template == 'reset' and coppa.is_coppa_covered(user.birth_month, user.birth_year):
        # Send email only to the sponsor address
        logging.info("COPPA account has a sponsor type of %s", user.parent_email_source)

        # Send password reset to student and parent
        send_email_template_to_address(user.email, 'reset-coppa', server, user.locale, params)
        send_email_template_to_address(user.parent_email, 'reset-coppa', server, user.locale, params)
        return
    else:
        # Registration not subject to COPPA regulations.
        #
        # Evaluate user wanting to use an alternate email address to register
        # the account.
        logging.info('Non-COPPA registration')
        if user.parent_email_source == SponsorType.INDIVIDUAL and user.parent_email:
            user_email = user.parent_email
            logging.info('Individual sponsor email %s being used', user_email)

        if user.parent_email:
            user_email = user.parent_email
            logging.info('Sponsor email %s being used', user_email)

        send_email_template_to_address(user_email, template, server, user.locale, params)

    return


def send_email_template_to_address(recipient, template, server, locale, params=None, **kwargs):
    logging.info("Preparing email template: %s for %s", template, recipient)
    params = params or {}

    # Add any supplied arguments to the parameter dictionary
    for key, value in kwargs.items():
        params[key] = value

    params['email'] = recipient
    params['locale'] = locale

    # Create a URI-friendly version of the email addresses
    params['email-uri'] = _convert_email_uri(params['email'])
    logging.info("Email address %s converted to %s",
                 params['email'],
                 params['email-uri']
                 )

    params['registrant-email-uri'] = _convert_email_uri(params['registrant-email'])
    logging.info("Registrant email address %s converted to %s",
                 params['registrant-email'],
                 params['registrant-email-uri']
                 )

    params['sponsor-email-uri'] = _convert_email_uri(params['sponsoremail'])
    logging.info("Sponsor email address %s converted to %s",
                 params['sponsoremail'],
                 params['sponsor-email-uri']
                 )

    # Read templates
    (subject, plain, rich) = _read_templates(template, server, locale, params)
    # Add error checking here to detect any issues with parsing the template.

    logging.info("Sending email to %s", params['email'])
    send_email(recipient, subject, plain, rich)


def send_email(recipient, subject, email_text, rich_email_text=None):
    try:
        _aws_send_mail(recipient, subject.rstrip(), email_text, rich_email_text)
        logging.info('Email message was delivered to server')
    except Exception as ex:
        logging.error('Unable to send email')
        logging.error('Error message: %s', ex.args)


def _read_templates(template, server, locale, params):
    logging.info("Loading header text for template: %s", template)
    header = _read_template(template, server, locale, 'header', params)

    logging.info("Loading plain message text for template: %s", template)
    plain = _read_template(template, server, locale, 'plain', params)

    logging.info("Loading rich message text for template: %s", template)
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
    template_file = str.format("{0}/{1}/{2}/{3}/{4}.mustache",
                               app.config['CLOUD_SESSION_PROPERTIES']['email.template.path'],
                               locale, template, server, part)
    logging.info("Template filename: %s", template_file)

    if isfile(template_file):
        logging.debug('Looking for template file: %s', template_file)
        renderer = pystache.Renderer()

        try:
            logging.debug('Rendering the template file')
            rendered = renderer.render_path(template_file, params)
        except Exception as ex:
            logging.error('Unable to render template file %s', template_file)
            logging.error('Error message: %s', ex.args)
            return 'Template format error.'

        logging.debug('Returning rendered template file.')
        return rendered
    else:
        logging.warn('Looking for template file: %s, but the file is missing', template_file)
        if none_if_missing:
            return None
        else:
            return 'Template missing'


def _convert_email_uri(email):
    """
    Evaluate email address and replace any plus signs that may appear in the
    portion of the address prior to the '@' with the literal '%2B'.

    Standard web servers will convert any plus ('+') symbol to a space (' ')
    anywhere where they may appear in the URL. This will allow functions upstream
    to create a URI that contains an email address that, when submitted to a
    server, will not be replaced with a space character.
    """
    if email is not None:
        if "+" in email:
            return email.replace("+", "%2B")

    return email


def _aws_send_mail(recipient, subject='', text_msg='', html_msg=''):
    # The sender address must be verified by SES.
    sender = app.config['CLOUD_SESSION_PROPERTIES']['mail.from']
    sender_name = "BlockyProp Administrator"
    username_smtp = app.config['CLOUD_SESSION_PROPERTIES']['mail.user']
    password_smtp = app.config['CLOUD_SESSION_PROPERTIES']['mail.password']
    host = app.config['CLOUD_SESSION_PROPERTIES']['mail.host']
    port = app.config['CLOUD_SESSION_PROPERTIES']['mail.port']

    # Create message container - the correct MIME type is multipart/alternative.
    msg = MIMEMultipart('alternative')
    msg['Subject'] = subject
    msg['From'] = email.utils.formataddr((sender_name, sender))
    msg['To'] = recipient

    # Record the MIME types of both parts - text/plain and text/html.
    # According to RFC 2046, the last part of a multipart message, in this case
    # the HTML message, is best and preferred.
    if text_msg != "":
        logging.info("Adding text to message")
        part1 = MIMEText(text_msg, 'plain')
        msg.attach(part1)

    # TODO: This breaks if the html_msg is empty or None
    # if html_msg != "":
    #     logging.info("Adding html to message")
    #     part2 = MIMEText(html_msg, 'html')
    #     msg.attach(part2)

    # Try to send the message.
    server = smtplib.SMTP(host, port)
    server.ehlo()
    server.starttls()
    # stmplib docs recommend calling ehlo() before & after starttls()
    server.ehlo()
    server.login(username_smtp, password_smtp)

    logging.info("awsSender: %s", sender)
    logging.info("awsRecipient: %s", recipient)
    logging.info("awsMessage: %s", msg.as_string())
    server.sendmail(sender, recipient, msg.as_string())

    server.close()
