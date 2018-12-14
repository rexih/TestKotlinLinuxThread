package cn.rexih.android.testkotlinlinuxthread

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import cn.rexih.android.testkotlinlinuxthread.entity.CountInfo
import cn.rexih.android.testkotlinlinuxthread.jni.CountJni
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(),IMainView {


    private var presenter: MainPresenter? = null
    private var countAdapter: CountAdapter? = null
    private var subscriber: Disposable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        countAdapter = CountAdapter(this)
        rv_content.layoutManager = LinearLayoutManager(this)
        rv_content.adapter = countAdapter

        btn_stop.setOnClickListener{
            CountJni.destroy()
        }
        subscriber = Flowable.create({ emitter: FlowableEmitter<Boolean> ->
            btn_create.setOnClickListener {
                emitter.onNext(true)
            }
        }, BackpressureStrategy.BUFFER)
                //TODO
            .debounce(1000, TimeUnit.MILLISECONDS)
            .subscribe {
                presenter?.requestTask()
            }

        presenter = MainPresenter(this)
        presenter?.initDataSource()

    }


    override fun update(dataSet: ArrayList<CountInfo>) {
        countAdapter?.setList(dataSet)
        rv_content.scrollToPosition(0);
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.destroy()
        subscriber?.dispose()
        CountJni.destroy()
    }

}
