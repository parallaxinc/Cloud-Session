from cloudsession import db


class AuthenticationToken(db.Model):
    id = db.Column(db.BigInteger, primary_key=True)
    id_user = db.Column(db.BigInteger)
    browser = db.Column(db.String(200))
    validity = db.Column(db.DateTime)
    token = db.Column(db.String(200), unique=True)
    server = db.Column(db.String(1000))
    ipaddress = db.Column(db.String(200))

    def __init__(self):
        pass

    def __repr__(self):
        return '<AuthenticationToken %s:%s>' % (self.id_user, self.ipaddress)


class Test(db.Model):
    id = db.Column(db.BigInteger, primary_key=True)
    name = db.Column(db.String(200))

    def __init__(self, name):
        self.name = name
