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

from app import db, app

from app.RateLimiting.models import Bucket


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
