![](https://github.com/rexih/TestKotlinLinuxThread/blob/master/screenshot.png)
1. java层代码使用kotlin编写
2. 多线程在jni层使用pthread创建
3. 使用了pthread_mutex_t互斥锁
4. 使用了Global Referrence缓存java层的接口回调类，方便在jni的线程中回调通知java层数据变化
5. 使用了stl库的map
6. pthread_create时向Thread handler函数传递了对象参数。了解到native层和java层一样栈上的变量跨进程使用会出问题，需要使用堆分配的内存
7. 使用Rxjava2，使用debounce减缓数据向下游发送
8. 由于多开线程后数据变化速度较快，超过下游处理数据的速度，可以很直观的研究背压策略
9. 尝试了kotlin的Adapter的使用，确实会比较简洁
