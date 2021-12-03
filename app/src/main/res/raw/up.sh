file_name="adb_process.dex"

path1="`dirname $0`/$file_name"
path2="/data/data/com.omarea.gesture/files/$file_name"

origin_path=""
if [[ -e "$path1" ]];then
    origin_path="$path2"
elif [[ -e "$path2" ]]; then
    origin_path="$path2"
else
    echo 'adb_process.dex Not found!'
    exit 1
fi

cache_dir="/data/local/tmp"
target_path="$cache_dir/gesture_process.dex"

current_process=`pgrep -f gesture_process.dex`
if [[ ! "$current_process" == "" ]]
then
    echo 'Kill Current GestureProcess >>'
    kill -9 $current_process
fi

echo "Origin File: " $origin_path
echo "Target File: " $target_path
echo ''

cmd package compile -m speed com.omarea.gesture 1 > /dev/null

if [[ -e $origin_path ]]; then
    cp $origin_path $target_path
    nohup dalvikvm -cp $target_path Main >/dev/null 2>&1 &
    sleep 2
    nohup app_process -Djava.class.path=$target_path $cache_dir Main >/dev/null 2>&1 &
    sleep 5
    am broadcast -a com.omarea.gesture.ConfigChanged 1>/dev/null
    am broadcast -a com.omarea.gesture.AdbProcess
else
    echo "Gesture's adb_process.dex not found !"
fi

