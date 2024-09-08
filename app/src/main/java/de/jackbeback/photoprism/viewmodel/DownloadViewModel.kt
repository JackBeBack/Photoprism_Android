package de.jackbeback.photoprism.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DownloadViewModel: ViewModel() {
    companion object {
        val instance = DownloadViewModel()
    }

    private val _downloadState = MutableStateFlow(DownloadState.IDLE)
    val downloadState = _downloadState.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress = _downloadProgress.asStateFlow()

    fun updateProgress(new: Int){_downloadProgress.update { new }}
    fun updateState(new: DownloadState){
        _downloadState.update { new }
        if (new == DownloadState.SUCCESS){
            updateProgress(0)
        }
    }


}

enum class DownloadState{
    IDLE,
    DOWNLOADING,
    PAUSED,
    ERROR,
    SUCCESS
}