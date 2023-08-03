package com.hjc.wanandroid.ui.main

import com.hjc.wanandroid.base.IUiState
import com.hjc.wanandroid.model.bean.Article
import com.hjc.wanandroid.model.bean.Banner

/**
 * @author jianchong.hu
 * @create at 2022 12.12
 * @description: ② 然后根据具体逻辑定义页面的UIState和UiIntent。
 * 通过MainState将页面的不同状态封装起来，从而实现唯一可信数据源
 **/
data class MainState(val bannerUiState: BannerUiState, val detailUiState: DetailUiState) : IUiState

sealed class BannerUiState {
    object INIT : BannerUiState()
    data class SUCCESS(val models: List<Banner>) : BannerUiState()
}

sealed class DetailUiState {
    object INIT : DetailUiState()
    data class SUCCESS(val articles: Article) : DetailUiState()
}