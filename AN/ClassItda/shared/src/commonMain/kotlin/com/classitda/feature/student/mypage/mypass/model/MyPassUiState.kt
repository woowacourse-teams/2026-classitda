package com.classitda.feature.student.mypage.mypass.model

data class MyPassUiState(
    val selectedTab: MyPassTab = MyPassTab.IN_USE,
    val inUse: MyPassTabState = MyPassTabState.Initial,
    val expired: MyPassTabState = MyPassTabState.Initial,
)
