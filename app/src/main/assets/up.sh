cp /storage/emulated/0/Android/data/com.omarea.gesture/cache/adb_process.dex /data/local/tmp/gesture_process.dex
nohup app_process -Djava.class.path=/data/local/tmp/gesture_process.dex /data/local/tmp Main >/dev/null 2>&1 &
