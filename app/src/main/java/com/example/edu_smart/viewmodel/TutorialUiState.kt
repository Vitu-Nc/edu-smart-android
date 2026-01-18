package com.example.edu_smart.viewmodel

import com.example.edu_smart.model.VideoItem

sealed class TutorialUiState {
    object Idle : TutorialUiState()
    object Loading : TutorialUiState()
    data class Success(val videos: List<VideoItem>) : TutorialUiState()
    data class Error(val message: String) : TutorialUiState()
}
