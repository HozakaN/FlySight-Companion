package fr.hozakan.flysightble.model

sealed interface FileState {
    data object Nothing : FileState
    data object Loading : FileState
    data class Success(val content: String) : FileState
    data class Error(val message: String) : FileState
}