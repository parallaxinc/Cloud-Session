
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
    def is_coppa_covered(self, month, year):
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
