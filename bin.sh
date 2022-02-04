#!/bin/bash
APP_NAME="user"
# SERVICE_HOME="/home/ec2-user/api/user"
SERVICE_HOME="/data001/oddiadm/user"
JAR_NAME="$APP_NAME.jar"
SERVICE_NAME="$APP_NAME-api"
PATH_TO_JAR="$SERVICE_HOME/$JAR_NAME"
ENV="op"
JAVA_OPT="-D$SERVICE_NAME -Xms128m -Xmx256m -Xss256k -Duser.timezone=Asia/Seoul -Dspring.profiles.active=$ENV"

start(){
  echo "$SERVICE_NAME starting..."
  local PID=$(get_status)
  if [ -n "$PID" ]; then
    echo "$SERVICE_NAME is already running ..."
    exit 0
  fi
  nohup java -jar $JAVA_OPT $PATH_TO_JAR > ./proc.log &
  echo "$SERVICE_NAME started ..."
}

stop(){
  local PID=$(get_status)
  if [ -n "$PID" ]; then
      echo "$SERVICE_NAME stoping ..."
      kill $PID;
      echo "$SERVICE_NAME stopped ..."
  else
      echo "$SERVICE_NAME is not running ..."
  fi
}

status(){
  local PID=$(get_status)
  if [ -n "${PID}" ]; then
      echo "${PROC_NAME} is running"
  else
      echo "${PROC_NAME} is stopped"
  fi
}

get_status(){
  ps ux | grep java | grep $SERVICE_HOME | grep -v grep | awk '{print $2}'
}

case $1 in
  start)
      start
      ;;
  stop)
      stop
    ;;
  restart)
      stop
      sleep 4
      start
    ;;
  status)
      status
    ;;
  *)
    echo "Usage: $0 {start | stop | restart | status }"
esac
