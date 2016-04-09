# Statement for enabling the development environment
#DEBUG = True

# Define the application directory
import os
BASE_DIR = os.path.abspath(os.path.dirname(__file__))

defaults = {
    'database.url': 'mysql+mysqldb://cloudsession:cloudsession@localhost:3306/cloudsession',

    'bucket.failed-password.size': '3',
    'bucket.failed-password.input': '1',
    'bucket.failed-password.freq': '120000',

    'bucket.password-reset.size': '2',
    'bucket.password-reset.input': '1',
    'bucket.password-reset.freq': '1800000',

    'bucket.email-confirm.size': '2',
    'bucket.email-confirm.input': '1',
    'bucket.email-confirm.freq': '1800000'
}

# Define the database - we are working with
# SQLite for this example
#SQLALCHEMY_DATABASE_URI = 'sqlite:///' + os.path.join(BASE_DIR, 'app.db')
SQLALCHEMY_DATABASE_URI = 'mysql+mysqldb://cloudsession:cloudsession@localhost:3306/cloudsession'
SQLALCHEMY_TRACK_MODIFICATIONS = True
DATABASE_CONNECT_OPTIONS = {}

# Application threads. A common general assumption is
# using 2 per available processor cores - to handle
# incoming requests using one and performing background
# operations using the other.
THREADS_PER_PAGE = 2

