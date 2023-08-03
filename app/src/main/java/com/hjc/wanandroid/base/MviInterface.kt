package com.hjc.wanandroid.base

import androidx.annotation.Keep

/**
 *
 * @author jianchong.hu
 * @create at
 * @description: ① 我们可以用interface先定义一个抽象的UI State、events，event和intent是一个意思，都可以用来表示一次事件。
 **/

@Keep
interface IUiState

@Keep
interface IUiIntent //event

sealed class LoadUiIntent {
    data class Loading(var isShow: Boolean) : LoadUiIntent()
    object ShowMainView : LoadUiIntent()
    data class Error(val msg: String) : LoadUiIntent()
}