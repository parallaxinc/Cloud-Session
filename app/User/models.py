from app import db


class User(db.Model):
    id = db.Column(db.BigInteger, primary_key=True)
    email = db.Column(db.String(250), unique=True)
    password = db.Column(db.String(100))
    salt = db.Column(db.String(50))
    auth_source = db.Column(db.String(250))
    locale = db.Column(db.String(50))
    blocked = db.Column(db.Boolean)
    confirmed = db.Column(db.Boolean)
    screen_name = db.Column(db.String(250))

    # COPPA support
    birth_month = db.Column(db.INTEGER, nullable=False)
    birth_year = db.Column(db.INTEGER, nullable=False)
    parent_email = db.Column(db.String(250))
    parent_email_source = db.Column(db.INTEGER)

    def __init__(self):
        self.blocked = False
        self.confirmed = False

    def __repr__(self):
        return '<User %s>' % self.email


class ResetToken(db.Model):
    id = db.Column(db.BigInteger, primary_key=True)
    id_user = db.Column(db.BigInteger, db.ForeignKey('user.id'), unique=True)
    validity = db.Column(db.DateTime)
    token = db.Column(db.String(200), unique=True)


class ConfirmToken(db.Model):
    id = db.Column(db.BigInteger, primary_key=True)
    id_user = db.Column(db.BigInteger, db.ForeignKey('user.id'), unique=True)
    validity = db.Column(db.DateTime)
    token = db.Column(db.String(200), unique=True)
