from app import db


class Bucket(db.Model):
    id = db.Column(db.BigInteger, primary_key=True)
    id_user = db.Column(db.BigInteger, db.ForeignKey('user.id'))
    type = db.Column(db.String(200))
    content = db.Column(db.Integer)
    timestamp = db.Column(db.DateTime)

    __table_args__ = (db.UniqueConstraint('id_user', 'type', name='user_type_unique'),)
