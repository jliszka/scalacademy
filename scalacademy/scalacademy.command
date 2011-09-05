#!/bin/sh

if [ ! -d scala-2.8.1.final ]; then
    curl -O "http://www.scala-lang.org/downloads/distrib/files/scala-2.8.1.final.tgz"
    tar xvzf scala-2.8.1.final.tgz
fi

curl -O "https://raw.github.com/jliszka/scalacademy/master/start.scala"
scala-2.8.1.final/bin/scala -i start.scala
