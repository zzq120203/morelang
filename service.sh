#!/bin/bash

# 通用重启服务脚本
# 无参数关闭程序
# email : zzq120203@163.com
# v1.2
*
#``````````````````````````
#     ┏┓　　　┏┓
#    ┏┛┻━━━━━━┛┻┓
#    ┃          ┃
#    ┃     ━    ┃
#    ┃  ┳┛   ┗┳ ┃
#    ┃          ┃
#    ┃     ┻    ┃
#    ┃          ┃
#    ┗━┓　　　┏━┛
#      ┃　　　┃神兽保佑
#      ┃　　　┃代码无BUG！
#      ┃　　　┗━━━━━┓
#      ┃　　　　 　 ┣┓
#      ┃　　　　    ┃
#      ┗━┓┓┏━━━━┳┓┏━┛
#        ┃┫┫    ┃┫┫
#        ┗┻┛    ┗┻┛
#^^^^^^^^^^^^^^^^^^^^^^^^^^



##参数
#jar
JARName="morelang.jar"
#程序主类名
MCName="MMHandler"
#程序主类路径 "."为分隔符
MCPath="cn.ac.iie.Service."
#第三方包路径
libPath="lib"
#library路径
libraryPath="library"
#其他参数
confPath="configs/config.txt"
log4jPath="configs/log4j.properties"
logsPath="logs"


function __start() {
    pid=`jps | grep "$MCName" | awk  '{print $1}'`
    if [ -n "$pid" ]
    then
        echo "$MCName is runing"
    else    
        for i in `ls $libPath`;
        do 
            libs+="$libPath/$i:"
        done

        if [ ! -f "$JARName" ]; then
            echo "ERROR:$JARName is not exist"
            exit
        fi
        nohup java -Djava.library.path=$libraryPath -Dconfig=$confPath -Dlog4j.configuration=$log4jPath -Xms10g -Xmx20g -XX:+UseG1GC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:$logsPath/gc.log -cp $libs:$JARName $MCPath$MCName >> $logsPath/$MCName.log &
        
        echo "PID:`jps | grep "$MCName"`"
    fi
}

function __stop() {
    pid=`jps | grep "$MCName" | awk  '{print $1}'`
    if [ -n "$pid" ]
    then
        kill $pid
        echo "kill PID : $pid"
    else
        echo "$MCName is not running"
    fi
}

function __reboot() {
    __stop;
    
    i=`jps | grep "$MCName" | wc -l`
    while [[ "$i" -ne 0 ]]
    do
        sleep 1
        echo "sleep stop PID : $pid" 
        i=`jps | grep "$MCName" | wc -l`
    done
    
    __start;
}

function __help() {
    echo "start  [jar] : start service"
    echo "stop         : stop service"
    echo "reboot [jar] : reboot service"
    exit
}

if [ "x$1" == "x" ]; then
    echo "ERROR:args is NULL"
    __help;
elif [ "x$1" == "xstart" ]; then
    echo "run $MCName"
    if [ ! "x$2" == "x" ]; then
        JARName=$2
    fi
    __start;
elif [ "x$1" == "xreboot" ]; then
    echo "reboot $MCName"
    if [ ! "x$2" == "x" ]; then
        JARName=$2
    fi
    __reboot;
elif [ "x$1" == "xstop" ]; then
    echo "stop $MCName"
    __stop;
else
    echo "ERROR:args is error"
    __help;
fi
