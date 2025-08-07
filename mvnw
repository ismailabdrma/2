#!/bin/bash

# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Maven Start Up Batch Script
#
# Required ENV vars:
# JAVA_HOME - location of a JDK home dir
# ----------------------------------------------------------------------------

# OS specific support.  $CYGWIN _must_ be set to 'true' to make this work.
cygwin=false
darwin=false
mingw=false
case "`uname`" in
  CYGWIN*) cygwin=true;;
  Darwin*) darwin=true;;
  MINGW*) mingw=true;;
esac

# For Cygwin, ensure paths are in UNIX format before anything else
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# For Darwin, add Java to path
if $darwin; then
  [ -z "$JAVA_HOME" ] && JAVA_HOME=/Library/Java/Home
  [ -z "$JAVA_HOME" ] && JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Home
fi

# Attempt to set JAVA_HOME if not already set
if [ -z "$JAVA_HOME" ]; then
  if [ -x "/usr/libexec/java_home" ]; then
    JAVA_HOME=`/usr/libexec/java_home`
  elif [ -d "/usr/java/latest" ]; then
    JAVA_HOME="/usr/java/latest"
  elif [ -d "/usr/lib/jvm/default-java" ]; then
    JAVA_HOME="/usr/lib/jvm/default-java"
  elif [ -d "/usr/lib/jvm/java-17-openjdk-amd64" ]; then # Common on Ubuntu/Debian
    JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
  elif [ -d "/opt/homebrew/opt/openjdk@17" ]; then # Common on macOS with Homebrew
    JAVA_HOME="/opt/homebrew/opt/openjdk@17"
  else
    echo "Error: JAVA_HOME is not set and no default Java installation found." >&2
    echo "Please set the JAVA_HOME environment variable to the path of your JDK installation." >&2
    exit 1
  fi
fi

# Check if JAVA_HOME is still empty or invalid
if [ -z "$JAVA_HOME" ] || [ ! -d "$JAVA_HOME" ]; then
  echo "Error: JAVA_HOME is not set or points to an invalid directory: $JAVA_HOME" >&2
  echo "Please set the JAVA_HOME environment variable to the path of your JDK installation." >&2
  exit 1
fi

# Set the Java executable path
JAVACMD="$JAVA_HOME/bin/java"
if [ ! -x "$JAVACMD" ]; then
  echo "Error: JAVA_HOME is set to an invalid directory. Could not find $JAVACMD" >&2
  echo "Please set the JAVA_HOME environment variable to the path of your JDK installation." >&2
  exit 1
fi

# Determine the location of the Maven wrapper JAR
PRG="$0"
PRGDIR=`dirname "$PRG"`
MAVEN_WRAPPER_JAR="$PRGDIR/.mvn/wrapper/maven-wrapper.jar"

# Check if the wrapper JAR exists
if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
  echo "Error: Maven wrapper JAR not found at $MAVEN_WRAPPER_JAR" >&2
  echo "Please ensure the .mvn/wrapper directory and maven-wrapper.jar exist." >&2
  exit 1
fi

# Execute the Maven wrapper
exec "$JAVACMD" -jar "$MAVEN_WRAPPER_JAR" "$@"
