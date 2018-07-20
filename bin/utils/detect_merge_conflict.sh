#!/bin/bash

# grep for '<<<<<<< HEAD'
grep -RUIl '<<<<<<< HEAD' src

if [ $? -ne 1 ]; then
    echo "src contain merge conflicts '<<<<<<< HEAD'. Please remove it and try again."
    exit 1;
fi
