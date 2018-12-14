#include "include/cn_rexih_android_testkotlinlinuxthread_jni_CountJni.h"

typedef struct java_class {
    jclass classOnDataChangedListener;
} JavaClass;

static JavaClass g_java_class;
typedef struct global_cache {
    jobject callback;
    jmethodID methodOnChanged;

    void (*pNotifyCallback)(JNIEnv *, const char *, int, int) = notifyCallback;

    JavaVM *vm;
    pthread_mutex_t lock;
    std::map<pthread_t, int *> *threadMap;
} GlobalCache;

struct ThreadInfo {
    int *cancelFlag;
    const char *threadName;
};
static GlobalCache g_cache;

void *JNICALL executeCount(void *threadInfo);

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    JNIEnv *env = NULL;
    jint result = -1;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)
        return result;

    g_cache.vm = vm;

    g_java_class.classOnDataChangedListener = (jclass) env->NewGlobalRef(
            env->FindClass("cn/rexih/android/testkotlinlinuxthread/jni/OnDataChangedListener"));
    g_cache.methodOnChanged = env->GetMethodID(g_java_class.classOnDataChangedListener, "onChanged",
                                               "(Ljava/lang/String;II)V");
    return JNI_VERSION_1_6;
}


void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return;
    }
    std::map<pthread_t, int *> *map = g_cache.threadMap;
    for (std::map<pthread_t, int *>::iterator itor = map->begin(); itor != map->end(); itor++) {
        delete itor->second;
    }
    delete g_cache.threadMap;
    g_cache.threadMap = NULL;
    pthread_mutex_destroy(&g_cache.lock);
    releaseGlobalCache(env);
}



/*
 * Class:     cn_rexih_android_testkotlinlinuxthread_jni_CountJni
 * Method:    setCallback
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_cn_rexih_android_testkotlinlinuxthread_jni_CountJni_setCallback
        (JNIEnv *env, jobject thiz, jobject callback) {
    g_cache.callback = env->NewGlobalRef(callback);

    g_cache.threadMap = new std::map<pthread_t, int *>();
    // 初始化线程锁
    pthread_mutex_init(&g_cache.lock, NULL);

}


/*
 * Class:     cn_rexih_android_testkotlinlinuxthread_jni_CountJni
 * Method:    createTask
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_cn_rexih_android_testkotlinlinuxthread_jni_CountJni_createTask
        (JNIEnv *env, jobject thiz, jstring tag) {
    // 上互斥锁
    pthread_mutex_lock(&g_cache.lock);


    pthread_attr_t threadAttr_;
    pthread_attr_init(&threadAttr_);
    pthread_attr_setdetachstate(&threadAttr_, PTHREAD_CREATE_DETACHED);


    pthread_t tid;
    int *pCancelFlag = new int;
    *pCancelFlag = 0;
    ThreadInfo *info = new ThreadInfo();
    info->cancelFlag = pCancelFlag;
    info->threadName = env->GetStringUTFChars(tag, NULL);

    int result = pthread_create(&tid, &threadAttr_, executeCount, info);

    g_cache.threadMap->insert(std::pair<pthread_t, int *>(tid, pCancelFlag));
    pthread_attr_destroy(&threadAttr_);

    //解互斥锁
    pthread_mutex_unlock(&g_cache.lock);
}
/*
 * Class:     cn_rexih_android_testkotlinlinuxthread_jni_CountJni
 * Method:    destroy
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_rexih_android_testkotlinlinuxthread_jni_CountJni_destroy
        (JNIEnv *env, jobject thiz) {
    // 停止线程 修改所有线程的停止标记
    std::map<pthread_t, int *> *map = g_cache.threadMap;
    for (std::map<pthread_t, int *>::iterator itor = map->begin(); itor != map->end(); itor++) {
        *(itor->second) = 1;
    }
}


void JNICALL notifyCallback(JNIEnv *env, const char *threadName, int count, int type) {
    env->CallVoidMethod(g_cache.callback, g_cache.methodOnChanged, env->NewStringUTF(threadName), count, type);
}

void JNICALL releaseGlobalCache(JNIEnv *env) {

    env->DeleteGlobalRef(g_java_class.classOnDataChangedListener);
    env->DeleteGlobalRef(g_cache.callback);
    g_cache.callback = NULL;
    g_cache.methodOnChanged = NULL;
    g_cache.pNotifyCallback = NULL;
    g_java_class.classOnDataChangedListener = NULL;
}

void *JNICALL executeCount(void *threadInfo) {

    ThreadInfo *pInfo = (ThreadInfo *) threadInfo;
    int *pCancelFlag = pInfo->cancelFlag;
    int a = *pCancelFlag;
    const char *cThreadName = pInfo->threadName;
    __android_log_print(ANDROID_LOG_FATAL, "rexih", "executeCount :test point2:%d:", a);
    __android_log_print(ANDROID_LOG_FATAL, "rexih", "executeCount :test point1:%s:", cThreadName);

    JavaVM *javaVM = g_cache.vm;
    JNIEnv *env;
    jint res = javaVM->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (res != JNI_OK) {
        res = javaVM->AttachCurrentThread(&env, NULL);
        if (JNI_OK != res) {
//            LOGE("Failed to AttachCurrentThread, ErrorCode = %d", res);
            return NULL;
        }
    }


    struct timespec sleepTime;
    sleepTime.tv_sec = 1;
    sleepTime.tv_nsec = 500 * 1000000;

    // 1. 初始化随机数
    srand((unsigned) time(NULL));
    int lbound = (rand() % (900 - 100)) + 100;
    // 2. 发送开始消息
    g_cache.pNotifyCallback(env, cThreadName, lbound, 1001);

    int ubound = lbound + 100;
    for (int i = lbound; i < ubound; i++) {
        pthread_mutex_lock(&g_cache.lock);
        int currentFlag = *pCancelFlag;
        pthread_mutex_unlock(&g_cache.lock);

        __android_log_print(ANDROID_LOG_FATAL, "rexih", "excuteCount :thread:%s :flag:%d :count:%d", cThreadName,
                            currentFlag, i);
        if (currentFlag > 0) {
            __android_log_print(ANDROID_LOG_FATAL, "rexih", "executeCount :thread:%s end", cThreadName);
            break;
        }

        // 3. 发送更新消息
        g_cache.pNotifyCallback(env, cThreadName, i, 1002);
        // 休息0.3秒
        nanosleep(&sleepTime, NULL);
    }
    // 4. 发送停止消息
    g_cache.pNotifyCallback(env, cThreadName, 0, 1003);

    javaVM->DetachCurrentThread();
    delete pInfo;
    return NULL;
}