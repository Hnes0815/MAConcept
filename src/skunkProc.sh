#!/bin/bash

# (2.5) skunk
  echo "SKUNK Durchlauf beginnt"

  mkdir $1
  cd $1
  echo "Verzeichnis gewechselt: $(pwd)"
  source=$2
  config=$3
  java -jar /home/hnes/git/MAConcept/exeskunk.jar --processed $source --config $config
  echo "SKUNK Durchlauf beendet"
