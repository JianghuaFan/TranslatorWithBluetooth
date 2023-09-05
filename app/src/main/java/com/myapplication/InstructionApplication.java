package com.myapplication;

import android.app.Application;

import com.rokid.glass.instruct.VoiceInstruction;

public class InstructionApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化语音指令SDK，App运行时默认关闭百灵鸟
        VoiceInstruction.init(this, false);
    }
}
