#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

JAR_NAME="ui-cli-1.0-SNAPSHOT.jar"

RELATIVE_JAR_PATH="ui-cli/target/$JAR_NAME"

JAR_PATH="$SCRIPT_DIR/$RELATIVE_JAR_PATH"

if [ ! -f "$JAR_PATH" ]; then
  echo "Error: JAR file not found at path: $JAR_PATH" >&2
  echo "Make sure the project has been built (e.g., using 'mvn clean package')," >&2
  echo "and the script '$0' is located in the main project directory." >&2
  exit 1
fi

java -jar "$JAR_PATH" "$@"