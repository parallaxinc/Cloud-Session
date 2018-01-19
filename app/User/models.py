from app import db


class User(db.Model):
    """
    User madel provides a direct mapping to the underlying
    database.
    
    Version 1 deploys the following fields:
        id          (BigInteger) - Unique record identifier
        email       (String(250)) - User email address
        password    (String(100)) - User account password
        salt        (String(50)) - A hash used to encrypt password 
        auth_source (String(250)) - Identifier for system providing authentication
        locale      (String(50)) - User language
        blocked     (Boolean) - Flag to indicate account is disabled
        confirmed   (Boolean) - Flag to indicate account has been verified
        screen_name (String(250)) - Unique user screen name
        
    Version 2 adds support for US COPPA compliance. The following fields were added:
        birth_month (INTEGER) - User birth month
        birth_year  (INTEGER) - User birth year
        parent_email (String(250)) - Sponsor email address
        parent_email_source (INTEGER) - Classification of sponsor email address

    """
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
        self.version = 2

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
