#!/bin/sh

PWD=$(dirname "$0")
echo $PWD
SCALA="$PWD/scala-2.8.1.final"

if [ ! -d $SCALA ]; then
    curl -o $SCALA.tgz "http://www.scala-lang.org/downloads/distrib/files/scala-2.8.1.final.tgz"
    tar xvzf $SCALA.tgz
fi

curl -o "$PWD/start.scala" "https://raw.github.com/jliszka/scalacademy/master/start.scala"
$SCALA/bin/scala -i "$PWD/start.scala"
