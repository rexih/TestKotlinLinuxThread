package cn.rexih.android.testkotlinlinuxthread

import android.content.Context
import android.graphics.Color
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.rexih.android.testkotlinlinuxthread.entity.CountInfo
import kotlinx.android.synthetic.main.item_main_count.view.*


/**
 *
 * @package cn.rexih.android.testkotlinlinuxthread
 * @file CountAdapter
 * @date 2018/11/24
 * @author huangwr
 * @version %I%, %G%
 */
class CountAdapter : RecyclerView.Adapter<CountAdapter.ViewHolder>  {
    var dataSet: ArrayList<CountInfo> = ArrayList()
    var context: Context? = null

    constructor(context: Context) : super() {
        this.context = context
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_main_count, p0, false))
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, p1: Int) {
        val data = dataSet[p1]
        with(holder.itemView) {
            val count = data?.count
            val color = countColor(count)
            tv_item_count_name?.text = data.threadName
            tv_item_count_content?.text = count.toString()
            tv_item_count_content?.setTextColor(color)
            tv_item_count_name?.setTextColor(color)
        }
    }

    fun countColor(count : Int): Int {
        val x = count / 100;
        val y = (count - x * 100) / 10
        val z = count - x * 100 - y * 10
        val r = x*50%256
        val g = y*50%256
        val b = z*50%256
        return Color.rgb(r, g, b)

    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    fun setList(newDataSet: ArrayList<CountInfo>){
        val calculateDiff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(p0: Int, p1: Int): Boolean {
                return dataSet[p0].threadName == newDataSet[p1].threadName
            }

            override fun getOldListSize(): Int {
                return dataSet.size
            }

            override fun getNewListSize(): Int {
                return newDataSet.size
            }

            override fun areContentsTheSame(p0: Int, p1: Int): Boolean {
                return dataSet[p0].count == newDataSet[p1].count
            }
        }, true)
        calculateDiff.dispatchUpdatesTo(this)
        dataSet.clear()
        dataSet.addAll(newDataSet)
    }
}