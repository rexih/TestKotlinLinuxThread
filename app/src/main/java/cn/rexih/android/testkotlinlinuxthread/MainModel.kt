package cn.rexih.android.testkotlinlinuxthread

import android.text.TextUtils
import android.util.Log
import cn.rexih.android.testkotlinlinuxthread.entity.CountInfo
import cn.rexih.android.testkotlinlinuxthread.jni.CountJni
import cn.rexih.android.testkotlinlinuxthread.jni.OnDataChangedListener
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


/**
 *
 * @package cn.rexih.android.testkotlinlinuxthread
 * @file MainModel
 * @date 2018/11/24
 * @author huangwr
 * @version %I%, %G%
 */
class MainModel {
    private val dataSet: ArrayList<CountInfo> = ArrayList()

    fun createDataSource(): Flowable<ArrayList<CountInfo>> {
        dataSet.clear()
        return Flowable.create(FlowableOnSubscribe<CountInfo> {
            CountJni.setCallback(object : OnDataChangedListener {
                override fun onChanged(threadName: String, count: Int, type: Int) {
                    if (!it.isCancelled) {
                        Log.i("rexih2","${threadName} : ${count} : ${type}")
                        it.onNext(CountInfo(threadName, count, type))
                    }
                }
            })
        }, BackpressureStrategy.BUFFER)
            .flatMap {
                Flowable.just(handle(it))
            }
            .debounce(300, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    @Synchronized
    private fun handle(info: CountInfo): ArrayList<CountInfo> {
        if (info.type == OnDataChangedListener.TYPE_CREATE) {
            dataSet.add(0, info)
            return dataSet
        }
        val itor = dataSet.listIterator()
        var countInfo: CountInfo
        var hit = false
        while (itor.hasNext()) {
            countInfo = itor.next()
            if (TextUtils.equals(info.threadName, countInfo.threadName)) {
                hit = true
                when (info.type) {
                    OnDataChangedListener.TYPE_UPDATE -> itor.set(info)
                    OnDataChangedListener.TYPE_DESTROY -> itor.remove()
                }
                break
            }
        }
        if (!hit) {
            info.type = OnDataChangedListener.TYPE_CREATE
            dataSet.add(0, info)
        }
        return dataSet
    }

    fun destroy() {
        CountJni.setCallback(null)
    }
}