#!/bin/bash

# This utilizes Maven to copy required dependencies and build the project
mvn clean
mvn dependency:copy-dependencies
mvn package
