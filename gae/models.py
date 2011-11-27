from google.appengine.ext import db

class User(db.Model):
    user = db.UserProperty()
    secret = db.IntegerProperty()
    level = db.StringProperty()
    step = db.IntegerProperty()

class Log(db.Model):
    user = db.ReferenceProperty(User)
    level = db.StringProperty()
    step = db.IntegerProperty()
    correct = db.BooleanProperty()
    time = db.DateTimeProperty(auto_now_add=True)




