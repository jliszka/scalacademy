from google.appengine.ext import db

class Log(db.Model):
    user = db.UserProperty()
    id = db.IntegerProperty()
    level = db.StringProperty()
    step = db.IntegerProperty()
    correct = db.BooleanProperty()
    time = db.DateTimeProperty(auto_now_add=True)

class Counter(db.Model):
    value = db.IntegerProperty(default=0)

