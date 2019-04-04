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

import datetime


# Enumerate the sponsor types for COPPA eligible user accounts
class SponsorType:
    INDIVIDUAL = 0
    PARENT = 1
    GUARDIAN = 2
    TEACHER = 3

    def __init__(self):
        pass


class Coppa:
    def __init__(self):
        pass

    # Return true if the date is less than 13 years
    @staticmethod
    def is_coppa_covered(month, year):
        # This is the number of months a typical thirteen years old has been on planet Earth.
        cap = 156

        # This is the actual number of months a typical user has been on the same planet.
        user_age = (year * 12) + month

        # Current year and month
        current_month = datetime.date.today().month
        current_year = datetime.date.today().year

        # This represents the number of months since the inception of AD
        # Unless you want to count that first year as part of BC.
        current_cap = (current_year * 12) + current_month

        if current_cap - user_age > cap:
            return False
        else:
            return True
