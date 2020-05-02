package com.omarea.gesture.util;

import android.app.ActivityManager;
import android.content.Context;

public class Memory {
    public int getMemorySizeMB(Context context){
        //获得ActivityManager服务的对象
        ActivityManager mActivityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        //获得MemoryInfo对象
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo() ;
        //获得系统可用内存，保存在MemoryInfo对象上
        mActivityManager.getMemoryInfo(memoryInfo) ;
        return (int) (memoryInfo.totalMem / 1024 / 1024);
    }
}
