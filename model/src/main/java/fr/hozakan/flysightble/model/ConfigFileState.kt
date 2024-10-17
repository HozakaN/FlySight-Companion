package fr.hozakan.flysightble.model

sealed interface ConfigFileState {
    data object Nothing : ConfigFileState
    data object Loading : ConfigFileState
    data class Success(val config: ConfigFile) : ConfigFileState
    data class Error(val message: String) : ConfigFileState
}