#!/bin/bash

# Comments
# - Customize for your installation, for instance you might want to add default parameters like the following:
# java -jar `dirname $0`/lib/confluence-cli-4.0.0-SNAPSHOT.jar --server https://my.example.com --user automation --password automation "$@"

java -jar `dirname $0`/lib/confluence-cli-4.0.0-SNAPSHOT.jar "$@"
