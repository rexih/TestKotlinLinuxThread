package cn.rexih.android.testkotlinlinuxthread

import cn.rexih.android.testkotlinlinuxthread.entity.CountInfo


/**
 *
 * @package cn.rexih.android.testkotlinlinuxthread
 * @file IMainView
 * @date 2018/11/24
 * @author huangwr
 * @version %I%, %G%
 */
interface IMainView {
    fun update(dataSet: ArrayList<CountInfo>)
}