package com.hjc.wanandroid.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 *
 * @author jianchong.hu
 * @create at 2022 12.12
 * @description: ③ 3.2 构建单向数据流UDF  在ViewModel中使用StateFlow构建UI State流。
    _uiStateFlow用来更新数据
    uiStateFlow用来暴露给UI elements订阅
 **/
abstract class BaseViewModel<UiState : IUiState, UiIntent : IUiIntent> : ViewModel() {
    //_uiStateFlow是一个MutableStateFlow，用于存储当前的UI状态。
    private val _uiStateFlow = MutableStateFlow(initUiState())

    //uiStateFlow用来暴露给UI elements订阅  uiStateFlow是一个只读的StateFlow，通过将_uiStateFlow暴露出去，
    // 可以让UI元素订阅并监听UI状态的变化。
    val uiStateFlow: StateFlow<UiState> = _uiStateFlow

    protected abstract fun initUiState(): UiState

    /*
    sendUiState()函数：

这是一个受保护的函数，用于发送新的UI状态。
sendUiState()接受一个lambda表达式参数copy，该lambda表达式对UiState执行一个复制操作，产生一个新的UiState。
然后，通过_uiStateFlow.update { ... }来更新_uiStateFlow的值，将新的UiState对象传递给它。
     */
    protected fun sendUiState(copy: UiState.() -> UiState) {
        //_uiStateFlow用来更新数据
        _uiStateFlow.update { copy(_uiStateFlow.value) }
    }

    //_uiIntentFlow是一个Channel，用于传输UI意图（Intent）。Channel类似于一个队列，
    // 适用于实现单个生产者（发送意图）和单个消费者（处理意图）之间的通信。
    private val _uiIntentFlow: Channel<UiIntent> = Channel()

    //uiIntentFlow是一个Flow，通过将_uiIntentFlow暴露为Flow，允许其他地方观察和订阅UI意图的流。
    val uiIntentFlow: Flow<UiIntent> = _uiIntentFlow.receiveAsFlow()

    private val _loadUiIntentFlow: Channel<LoadUiIntent> = Channel()
    val loadUiIntentFlow: Flow<LoadUiIntent> = _loadUiIntentFlow.receiveAsFlow()

    //这是一个公共函数，用于发送UI意图。当调用sendUiIntent()时，它会将意图发送到_uiIntentFlow通道中。
    //使用viewModelScope.launch启动一个新的协程，在该协程中发送UI意图。
    fun sendUiIntent(uiIntent: UiIntent) {
        viewModelScope.launch {
            _uiIntentFlow.send(uiIntent)
        }
    }

    //在BaseViewModel的构造函数中，启动了一个协程来监听uiIntentFlow。
    // 这个协程通过collect操作来消费通道中的UI意图，并通过handleIntent()函数处理UI意图。
    init {
        viewModelScope.launch {
            uiIntentFlow.collect {
                handleIntent(it)
            }
        }
    }

    //在BaseViewModel中，定义了一个抽象的handleIntent()函数，用于在子类中处理UI意图。子类需要实现这个函数，根据具体的UI意图执行不同的操作。
    protected abstract fun handleIntent(intent: IUiIntent)

    /**
     * 发送当前加载状态：Loading、Error、Normal
     */
    private fun sendLoadUiIntent(loadUiIntent: LoadUiIntent) {
        viewModelScope.launch {
            _loadUiIntentFlow.send(loadUiIntent)
        }
    }

    //其中 requestDataWithFlow 是封装的一个网络请求的方法
    protected fun <T : Any> requestDataWithFlow(
        showLoading: Boolean = true,
        request: suspend () -> BaseData<T>,
        successCallback: (T) -> Unit,
        failCallback: suspend (String) -> Unit = { errMsg ->
            //默认异常处理，子类可以进行覆写
            sendLoadUiIntent(LoadUiIntent.Error(errMsg))
        },
    ) {
        viewModelScope.launch {
            //是否展示Loading
            if (showLoading) {
                sendLoadUiIntent(LoadUiIntent.Loading(true))
            }
            val baseData: BaseData<T>
            try {
                baseData = request()
                when (baseData.state) {
                    ReqState.Success -> {
                        sendLoadUiIntent(LoadUiIntent.ShowMainView)
                        baseData.data?.let { successCallback(it) }
                    }
                    ReqState.Error -> baseData.msg?.let { error(it) }
                }
            } catch (e: Exception) {
                e.message?.let { failCallback(it) }
            } finally {
                if (showLoading) {
                    sendLoadUiIntent(LoadUiIntent.Loading(false))
                }
            }
        }
    }
}