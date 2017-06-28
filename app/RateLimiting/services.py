import datetime

from app import db, app

from models import Bucket


# User needs to be validated to be existing
def consume_tokens(id_user, bucket_type, token_count):
    bucket = Bucket.query.filter_by(id_user=id_user, type=bucket_type).first()

    bucket_size = int(app.config['CLOUD_SESSION_PROPERTIES']['bucket.%s.size' % bucket_type]) or 0
    bucket_stream_input = int(app.config['CLOUD_SESSION_PROPERTIES']['bucket.%s.input' % bucket_type]) or 0
    bucket_stream_frequency = int(app.config['CLOUD_SESSION_PROPERTIES']['bucket.%s.freq' % bucket_type]) or 1000

    if bucket is None:
        bucket = Bucket()
        bucket.id_user = id_user
        bucket.type = bucket_type
        bucket.content = bucket_size
        bucket.timestamp = datetime.datetime.now()

        db.session.add(bucket)
        db.session.flush()
        db.session.refresh(bucket)

        old_bucket_content = bucket_size
    else:
        old_bucket_content = bucket.content

        elapsed_milliseconds = (datetime.datetime.now() - bucket.timestamp).total_seconds() * 1000
        input_count = elapsed_milliseconds / bucket_stream_frequency
        bucket.content = int(min(bucket_size, (input_count * bucket_stream_input) + bucket.content))

    if bucket.content < token_count:
        milliseconds_till_enough = (token_count - old_bucket_content) * bucket_stream_frequency
        date_when_enough = bucket.timestamp + datetime.timedelta(milliseconds=milliseconds_till_enough)
        # Log and return or throw error
        return False, date_when_enough

    bucket.content = bucket.content - token_count
    bucket.timestamp = datetime.datetime.now()
    return True,  bucket.timestamp


def has_sufficient_tokens(id_user, bucket_type, token_count):
    bucket = Bucket.query.filter_by(id_user=id_user, type=bucket_type).first()

    bucket_size = int(app.config['CLOUD_SESSION_PROPERTIES']['bucket.%s.size' % bucket_type]) or 0
    bucket_stream_input = int(app.config['CLOUD_SESSION_PROPERTIES']['bucket.%s.input' % bucket_type]) or 0
    bucket_stream_frequency = int(app.config['CLOUD_SESSION_PROPERTIES']['bucket.%s.freq' % bucket_type]) or 1000

    if bucket is None:
        bucket = Bucket()
        bucket.id_user = id_user
        bucket.type = bucket_type
        bucket.content = bucket_size
        bucket.timestamp = datetime.datetime.now()

        old_bucket_content = bucket_size
        new_bucket_content = bucket_size
    else:
        old_bucket_content = bucket.content

        elapsed_milliseconds = (datetime.datetime.now() - bucket.timestamp).total_seconds() * 1000
        input_count = elapsed_milliseconds / bucket_stream_frequency
        new_bucket_content = int(min(bucket_size, (input_count * bucket_stream_input) + bucket.content))

    if new_bucket_content < token_count:
        milliseconds_till_enough = (token_count - old_bucket_content) * bucket_stream_frequency
        date_when_enough = bucket.timestamp + datetime.timedelta(milliseconds=milliseconds_till_enough)
        # Log and return or throw error
        return False, date_when_enough

    return True
