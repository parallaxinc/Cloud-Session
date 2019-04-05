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

"""
User REST API

The url_prefix is '/user'. All user endpoints are in the form
# of host:port/user/_service_
#


# Register a new user account
/user/register


        server = request.headers.get('server')
        email = request.form.get('email')
        password = request.form.get('password')
        password_confirm = request.form.get('password-confirm')
        locale = request.form.get('locale')
        screen_name = request.form.get('screenname')

        # COPPA support
        birth_month = request.form.get('bdmonth')
        birth_year = request.form.get('bdyear')
        parent_email = request.form.get('parent-email')
        parent_email_source = request.form.get('parent-email-source')


# Retrieve details about an existing user account
api.add_resource(GetUserById, '/id/<int:id_user>')



api.add_resource(GetUserByEmail, '/email/<string:email>')



api.add_resource(GetUserByScreenname, '/screenname/<string:screen_name>')


# Update a user screen name
api.add_resource(DoInfoChange, '/info/<int:id_user>')


# Update the local defined in the user account
api.add_resource(DoLocaleChange, '/locale/<int:id_user>')

"""