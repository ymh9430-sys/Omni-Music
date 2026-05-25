#!/usr/bin/env sh

# System-specific vars
case "`uname`" in
  CYGWIN* | MINGW* | MSYS*) sys_os=Windows ;;
  *) sys_os=Other ;;
esac

# Find Theme/Gradle
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

exec "$JAVACMD" -jar "$0.jar" "$@"

