#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
import os
from google.appengine.ext import db
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp import util
from models import Log, User

class MainHandler(webapp.RequestHandler):
    def get(self):
        template_values = {}
        path = os.path.join(os.path.dirname(__file__), 'index.html')
        self.response.out.write(template.render(path, template_values))


class IncHandler(webapp.RequestHandler):
    def get(self):
        id = int(self.request.get('id'))
        secret = int(self.request.get('secret'))
        user = db.get(db.Key.from_path("User", id))
        if user.secret == secret:
            level = self.request.get('level')
            step = int(self.request.get('step'))
            correct = self.request.get('correct') == '1'
            log = Log(user=user, level=level, step=step, correct=correct)
            log.put()
            user.level = level
            user.step = step
            user.put()
        self.response.out.write("ok")

class IdHandler(webapp.RequestHandler):
    def get(self):
        secret = int(self.request.get('secret'))
        user = User(secret=secret)
        user.put()
        self.response.out.write(user.key().id())


class LastHandler(webapp.RequestHandler):
    def get(self):
        id = int(self.request.get('id'))
        secret = int(self.request.get('secret'))
        user = db.get(db.Key.from_path("User", id))
        if user.secret == secret:
            self.response.out.write("%s:%d" % (user.level, user.step))
        else:
            self.response.out.write("1:1")

def main():
    application = webapp.WSGIApplication([('/', MainHandler),
                                          ('/inc', IncHandler),
                                          ('/id', IdHandler),
                                          ('/last', LastHandler)],
                                         debug=True)
    util.run_wsgi_app(application)


if __name__ == '__main__':
    main()
