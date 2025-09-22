@echo off
setlocal
if not exist lib mkdir lib
mvn -q dependency:copy -Dartifact=com.h2database:h2:2.2.224 -DoutputDirectory=lib
java -cp lib\h2-2.2.224.jar org.h2.tools.Server -tcp -tcpAllowOthers -ifNotExists -tcpPort 9092
