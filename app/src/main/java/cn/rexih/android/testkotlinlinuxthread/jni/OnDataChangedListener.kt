package cn.rexih.android.testkotlinlinuxthread.jni


/**
 *
 * @package cn.rexih.android.testkotlinlinuxthread.jni
 * @file OnDataChangedListener
 * @date 2018/11/24
 * @author huangwr
 * @version %I%, %G%
 */
interface OnDataChangedListener {
    companion object {
        const val TYPE_CREATE = 1001
        const val TYPE_UPDATE = 1002
        const val TYPE_DESTROY = 1003
    }

    fun onChanged(threadName: String, count: Int, type: Int)
}