#!/bin/bash

echo "Start von "$0

echo "GIT CHECKOUT für "$2" beginnt"
cd $1						# in den Repo Ordner wechseln
echo "Verzeichnis gewechselt: $(pwd)"
git checkout $2				# Revision auschecken
