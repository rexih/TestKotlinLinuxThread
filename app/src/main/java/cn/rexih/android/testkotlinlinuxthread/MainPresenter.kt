package cn.rexih.android.testkotlinlinuxthread

import cn.rexih.android.testkotlinlinuxthread.entity.CountInfo
import cn.rexih.android.testkotlinlinuxthread.jni.CountJni
import io.reactivex.subscribers.DisposableSubscriber
import java.util.concurrent.atomic.AtomicInteger


/**
 *
 * @package cn.rexih.android.testkotlinlinuxthread
 * @file MainPresenter
 * @date 2018/11/24
 * @author huangwr
 * @version %I%, %G%
 */
class MainPresenter constructor(iView: IMainView) {

    companion object {
        const val THREAD_PREFIX = "Kotlin_Linux_%d"
    }

    private val iView: IMainView = iView
    private val model = MainModel()
    private val countThread = AtomicInteger()
    private var subscriber: DisposableSubscriber<ArrayList<CountInfo>>? = null

    fun destroy() {
        model.destroy()
        subscriber?.dispose()
    }

    @Synchronized
    fun requestTask() {
//        subscriber ?: initDataSource()
        CountJni.createTask(String.format(THREAD_PREFIX, countThread.incrementAndGet()))
    }

    fun initDataSource() {
        subscriber = object : DisposableSubscriber<ArrayList<CountInfo>>() {
            override fun onComplete() {
            }

            override fun onNext(t: ArrayList<CountInfo>) {
                iView.update(t)
            }

            override fun onError(t: Throwable?) {
            }
        }
        model.createDataSource()
            .subscribe(subscriber)
    }

}