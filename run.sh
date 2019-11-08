#!/bin/sh

# Causes the built project to run with provided argument

java -cp target/LightWeightLineServer-1.0-SNAPSHOT.jar:target/dependency/sqlite-jdbc-3.27.2.1.jar:target/dependency/http-20070405.jar kayomars.LightLineServer $1 
