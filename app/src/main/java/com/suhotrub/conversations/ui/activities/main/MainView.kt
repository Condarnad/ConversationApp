package com.suhotrub.conversations.ui.activities.main

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.suhotrub.conversations.model.group.GroupDto
import com.suhotrub.conversations.ui.util.recycler.PaginationState
import retrofit2.HttpException

interface MainView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun renderChats(list: List<GroupDto>)

    @StateStrategyType(SkipStrategy::class)
    fun showErrorSnackbar(t: Throwable)

    @StateStrategyType(SkipStrategy::class)
    fun setPaginationState(paginationState: PaginationState)
}