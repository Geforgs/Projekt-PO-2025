@echo off

SET "SCRIPT_DIR=%~dp0"

SET "JAR_NAME=ui-cli-1.0-SNAPSHOT.jar"

SET "RELATIVE_JAR_PATH=ui-cli\target\%JAR_NAME%"

SET "JAR_PATH=%SCRIPT_DIR%%RELATIVE_JAR_PATH%"

IF NOT EXIST "%JAR_PATH%" (
    echo Error: JAR file not found at path: "%JAR_PATH%"
    echo Make sure the project has been built (e.g., using 'mvn clean package'),
    echo and the script '%0' is located in the main project directory.
    EXIT /B 1
)

java -jar "%JAR_PATH%" %*