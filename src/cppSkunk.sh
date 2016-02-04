#!/bin/bash

# Pfad zu CPPSTATS
statspath="/home/hnes/Masterarbeit/Tools/cppstats/"

# (2) cppstats
  echo "CPPSTATS Durchlauf beginnt"
  cd $statspath					# in den cppstats Ordner wechseln
  echo "Verzeichnis gewechselt: $(pwd)"
  
  cppstats --kind general &			#cppstats anschmeißen und warten
  cppstats --kind featurelocations &
  wait

  echo "CPPSTATS Durchlauf abgeschlossen"
  
  # (2.5) skunk
  echo "SKUNK Durchlauf beginnt"

  mkdir $1
  cd $1
  echo "Verzeichnis gewechselt: $(pwd)"
  source=$2
  config=$3
  java -jar /home/hnes/git/MAConcept/exeskunk.jar --source $source --saveintermediate --config $config
  echo "SKUNK Durchlauf beendet"
