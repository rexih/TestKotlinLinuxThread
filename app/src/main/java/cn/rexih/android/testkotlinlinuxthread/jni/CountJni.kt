package cn.rexih.android.testkotlinlinuxthread.jni


/**
 *
 * @package cn.rexih.android.testkotlinlinuxthread.jni
 * @file CountJni
 * @date 2018/11/24
 * @author huangwr
 * @version %I%, %G%
 */
object CountJni {
    init {
        System.loadLibrary("native-lib")
    }

    external fun setCallback(callback: OnDataChangedListener?)

    external fun createTask(tag: String)

    external fun destroy()
}