# Import the database object (db) from the main application module
# We will define this inside /app/__init__.py in the next sections.
from app import db


class AuthenticationToken(db.Model):
    id = db.Column(db.BigInteger, primary_key=True)
    id_user = db.Column(db.BigInteger, db.ForeignKey('user.id'))
    browser = db.Column(db.String(200))
    server = db.Column(db.String(1000))
    ip_address = db.Column(db.String(200))
    validity = db.Column(db.DateTime)
    token = db.Column(db.String(200), unique=True)

    def __init__(self):
        pass

    def __repr__(self):
        return '<AuthenticationToken %s:%s>' % (self.id_user, self.ip_address)
