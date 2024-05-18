package com.blanco.somelai.ui.home.feed

import com.blanco.somelai.data.network.model.body.WineBody

data class WineBodyUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val response: List<WineBody>? = null
)
