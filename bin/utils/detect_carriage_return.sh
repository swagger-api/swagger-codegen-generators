#!/bin/bash


# grep for \r in the templates
grep -RUIl $'\r$' src/main/resources/*

if [ $? -ne 1 ]; then
    echo "Templates contain carriage return '/r'. Please remove it and try again."
    exit 1;
fi


# grep for \r in the generators
#grep -RUIl $'\r$' modules/swagger-codegen/src/main/java/io/swagger/codegen/v3/*.java
find src/main/java/ -type f -iname "*.java" -exec grep -RUIl $'\r$' {} \; | wc -l

if [ $? -ne 0 ]; then
    echo "Generators contain carriage return '/r'. Please remove it and try again."
    exit 1;
fi
