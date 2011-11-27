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
from models import Log, Counter

class MainHandler(webapp.RequestHandler):
    def get(self):
        template_values = {}
        path = os.path.join(os.path.dirname(__file__), 'index.html')
        self.response.out.write(template.render(path, template_values))


class IncHandler(webapp.RequestHandler):
    def get(self):
        id = int(self.request.get('id'))
        level = self.request.get('level')
        step = int(self.request.get('step'))
        correct = self.request.get('correct') == '1'
        log = Log(id=id, level=level, step=step, correct=correct)
        log.put()
        self.response.out.write('ok')

class IdHandler(webapp.RequestHandler):
    def get(self):
        def get_and_increment(key):
            counter = db.get(key)
            counter.value += 1
            counter.put()
            return counter.value
        counter = Counter.gql("").get()
        if counter == None:
            counter = Counter()
            counter.put()
        id = db.run_in_transaction(get_and_increment, counter.key())
        self.response.out.write(id)


def main():
    application = webapp.WSGIApplication([('/', MainHandler),
                                          ('/inc', IncHandler),
                                          ('/id', IdHandler)],
                                         debug=True)
    util.run_wsgi_app(application)


if __name__ == '__main__':
    main()
