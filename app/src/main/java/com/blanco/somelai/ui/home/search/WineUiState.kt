package com.blanco.somelai.ui.home.search

import com.blanco.somelai.data.network.model.responses.Wine

data class WineUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val response: List<Wine>? = null
)
