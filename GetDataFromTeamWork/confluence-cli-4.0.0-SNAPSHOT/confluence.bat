@echo off
rem remember the directory path to this bat file
set dirPath=%~dp0

rem need to reverse windows names to posix names by changing \ to /
set dirPath=%dirPath:\=/%
rem remove blank at end of string
set dirPath=%dirPath:~0,-1%

set server=
set username=
set password=


rem - Customize for your installation, for instance you might want to add default parameters like the following:
java -Xmx1g  -Xms512m -jar "%dirPath%"/lib/confluence-cli-4.0.0-SNAPSHOT.jar --server "%server%" --user "%username%" --password "%password%" %*