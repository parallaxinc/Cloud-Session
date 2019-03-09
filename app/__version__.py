#!/usr/bin/env python

"""
Change Log

1.3.0       Update all packages to current releases.
            Refactor to support Python 3.7

1.1.7       Update application logging to separate application events from
            those logged by the uwsgi servivce

1.1.6       Add email address detail for various authentication failures

1.1.5       Refactor _convert_email_uri(email) to properly handle a null
            email address.

1.1.4       Add code to convert plus signs located the the username portion
            of an email address to a '%2B'when the email address is embedded
            in a URL.

1.1.3       Added documentation around the user account registration process.

"""

__version__ = "1.3.0"
