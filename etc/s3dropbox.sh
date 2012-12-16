#!/bin/sh
cd $(dirname $0)
java -jar S3DropBox.jar -cli $@
