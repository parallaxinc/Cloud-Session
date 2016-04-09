from app import db

from flask_restful import Resource, Api
from flask import request, Blueprint

from Validation import Validation

from models import *

rate_limiting_app = Blueprint('rate', __name__, url_prefix='/rate')
api = Api(rate_limiting_app)
