#!/bin/sh

#check JAVA_HOME & java
noJavaHome=false
if [ -z "$JAVA_HOME" ] ; then
    noJavaHome=true
fi
if [ ! -e "$JAVA_HOME/bin/java" ] ; then
    noJavaHome=true
fi
if $noJavaHome ; then
    echo
    echo "Error: JAVA_HOME environment variable is not set."
    echo
    exit 1
fi
#==============================================================================
#set JAVA_OPTS
JAVA_OPTS="-server -Xms1G -Xmx2G -XX:MaxPermSize=128M  -XX:+AggressiveOpts -XX:MaxDirectMemorySize=1G"
#performance Options
#JAVA_OPTS="$JAVA_OPTS -Xss256k"
#JAVA_OPTS="$JAVA_OPTS -XX:+AggressiveOpts"
#JAVA_OPTS="$JAVA_OPTS -XX:+UseBiasedLocking"
#JAVA_OPTS="$JAVA_OPTS -XX:+UseFastAccessorMethods"
#JAVA_OPTS="$JAVA_OPTS -XX:+DisableExplicitGC"
#JAVA_OPTS="$JAVA_OPTS -XX:+UseParNewGC"
#JAVA_OPTS="$JAVA_OPTS -XX:+UseConcMarkSweepGC"
#JAVA_OPTS="$JAVA_OPTS -XX:+CMSParallelRemarkEnabled"
#JAVA_OPTS="$JAVA_OPTS -XX:+UseCMSCompactAtFullCollection"
#JAVA_OPTS="$JAVA_OPTS -XX:+UseCMSInitiatingOccupancyOnly"
#JAVA_OPTS="$JAVA_OPTS -XX:CMSInitiatingOccupancyFraction=75"
#JAVA_OPTS="$JAVA_OPTS -XX:CMSInitiatingOccupancyFraction=75"
#GC Log Options
#JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCApplicationStoppedTime"
#JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCTimeStamps"
#JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails"
#debug Options
#JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=8065,server=y,suspend=n"
# ZK
#JAVA_OPTS="$JAVA_OPTS -Djava.security.auth.login.config=/etc/security/apps_client_jaas.conf"
#==============================================================================

#set HOME
CURR_DIR=`pwd`
cd `dirname "$0"`/..
HOME=`pwd`
cd $CURR_DIR


NET_CLASSPATH="$HOME/conf:$HOME/lib/*"



#==============================================================================

#startup Server
RUN_CMD="$JAVA_HOME/bin/java"
RUN_CMD="$RUN_CMD -classpath $NET_CLASSPATH"
RUN_CMD="$RUN_CMD $JAVA_OPTS"
RUN_CMD="$RUN_CMD  com.seaboat.net.reactor.Bootstrap  $@"
RUN_CMD="$RUN_CMD > $HOME/logs/console.log 2>&1 &"
echo $RUN_CMD
eval $RUN_CMD
echo $! > $HOME/bin/1.pid
#==============================================================================
