package com.hjc.wanandroid.ui.main

import com.hjc.wanandroid.base.BaseViewModel
import com.hjc.wanandroid.base.IUiIntent
import com.hjc.wanandroid.model.respository.HomeRepository

/**
 *
 * @author jianchong.hu
 * @create at 2022 12.12
 * @description:
 **/
class MainViewModel(private val homeRepo: HomeRepository) : BaseViewModel<MainState, MainIntent>() {

    //initUiState()函数，用于初始化MainState，并指定了初始状态。
    override fun initUiState(): MainState {
        return MainState(BannerUiState.INIT, DetailUiState.INIT)
    }

    //，实现了handleIntent()函数，根据不同的UI意图执行相应的操作。
    // 例如，当收到MainIntent.GetBanner意图时，调用requestDataWithFlow()方法请求数据。
    //调用sendUiState发送Ui State更新
    //需要注意的是： 在UiState改变时，使用的是copy复制一份原来的UiState，
    // 然后修改变动的值。这是为了做到 “可信数据源”，在定义MainState的时候，设置的就是val，
    // 是为了避免多线程并发读写，导致线程安全的问题。

    override fun handleIntent(intent: IUiIntent) {
        when (intent) {
            MainIntent.GetBanner -> {
                requestDataWithFlow(showLoading = true,
                    request = { homeRepo.requestWanData() },
                    successCallback = { data ->
                        sendUiState {
                            copy(
                                bannerUiState = BannerUiState.SUCCESS(
                                    data
                                )
                            )
                        }
                    },
                    failCallback = {})
            }
            is MainIntent.GetDetail -> {
                requestDataWithFlow(showLoading = false,
                    request = { homeRepo.requestRankData(intent.page) },
                    successCallback = { data ->
                        sendUiState {
                            copy(
                                detailUiState = DetailUiState.SUCCESS(
                                    data
                                )
                            )
                        }
                    })
            }
        }
    }
}

