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

__version__ = "1.3.3"

"""
Change Log

1.3.3       Update packages subject to security warnings.
            Update sentry package.
            Replace Flask SMTP interface with the generic Python mail library.
            SMTP now relays directly to Amazon SES.
            Add /health endpoints to Rest API

1.3.2       Update supervisor package to accommodate the removal of the cgi
            library in python 3.8.
            Add API keys for version and ping.

1.3.1       Add error handling to password authentication to trap the possibility
            of an unencoded password submission.
            Update password hash methods to accommodate a change in default string
            handling in Python3.

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
