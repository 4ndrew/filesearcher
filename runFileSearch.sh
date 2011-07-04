#!/bin/sh

log4j=log4j.xml
javabin=java
javajar=out/artifacts/FileSearcher_jar/FileSearcher.jar 

javaopts=""
javaopts+=" -Dlog4j.configuration=${log4j}"
# javaopts+=" -Dlog4j.debug"
javaopts+=" -Dcom.sun.management.jmxremote"
javaopts+=" -Dcom.sun.management.jmxremote.ssl=false"

$javabin $javaopts -jar "$javajar" $@ 
# "$1" "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9"
