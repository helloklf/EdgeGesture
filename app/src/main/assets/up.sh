file_name="adb_process.dex"

path1="`dirname $0`/$file_name"
path2="/storage/emulated/0/Android/data/com.omarea.gesture/cache/$file_name"
path3="/sdcard/Android/data/com.omarea.gesture/cache/$file_name"
path4="/data/media/0/Android/data/com.omarea.gesture/cache/$file_name"

origin_path=""
if [[ -e "$path1" ]];then
    origin_path="$path2"
elif [[ -e "$path2" ]]; then
    origin_path="$path2"
elif [[ -e "$path3" ]]; then
    origin_path="$path3"
elif [[ -e "$path4" ]]; then
    origin_path="$path4"
else
    echo 'Unknown sdcard !'
    exit 1
fi

cache_dir="/data/local/tmp"
target_path="$cache_dir/gesture_process.dex"

echo "Origin File: " $origin_path
echo "Target File: " $target_path
echo ''

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

