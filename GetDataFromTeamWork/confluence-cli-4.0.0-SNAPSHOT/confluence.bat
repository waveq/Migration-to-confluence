@echo off
rem remember the directory path to this bat file
set dirPath=%~dp0

rem need to reverse windows names to posix names by changing \ to /
set dirPath=%dirPath:\=/%
rem remove blank at end of string
set dirPath=%dirPath:~0,-1%

set server=https://traineetest.atlassian.net/wiki/
set username=nynon
set password=tajnehaslo

rem - Customize for your installation, for instance you might want to add default parameters like the following:
java -jar "%dirPath%"/lib/confluence-cli-4.0.0-SNAPSHOT.jar --server "%server%" --user "%username%" --password "%password%" %*

java -jar "%dirPath%"/lib/confluence-cli-4.0.0-SNAPSHOT.jar %*

cd temp
del *.*
cd ..

rem Exit with the correct error level.
EXIT /B %ERRORLEVEL%