#!/bin/sh

printf "\e[8;50;100;t"

PWD=$(dirname "$0")
cd $PWD
SCALA="scala-2.8.1.final"

if [ ! -e $SCALA.tgz ]; then
    echo "Downloading Scala..."
    curl -O "http://www.scala-lang.org/downloads/distrib/files/$SCALA.tgz"
fi

if [ ! -d $SCALA ]; then
    tar xvzf $SCALA.tgz
fi

curl -O "https://raw.github.com/jliszka/scalacademy/master/start.scala"
$SCALA/bin/scala -i "$PWD/start.scala"
