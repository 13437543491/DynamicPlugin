#include <jni.h>
#include <string>
#include <unistd.h>
#include <android/log.h>

#define LOGE(TAG, ...) __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)

extern "C"
JNIEXPORT void JNICALL
Java_com_quick_dynamic_plugin_proxy_ActivityManagerServiceProxy_test(JNIEnv *env, jclass clazz) {
    pid_t pid = fork();
    if (pid < 0) {
        LOGE("yangyangyang", "pid<0");
        //创建失败
    } else if (pid == 0) {
        LOGE("yangyangyang", "pid=0");

        //子进程
        jclass jclass1 = env->FindClass(
                "com/quick/dynamic/plugin/proxy/ActivityManagerServiceProxy");
        jmethodID jmethodId = env->GetStaticMethodID(jclass1, "test2", "()V");
        env->CallStaticVoidMethod(jclass1, jmethodId);
    } else {
        LOGE("yangyangyang", "pid>0");

        //父进程
    }
}