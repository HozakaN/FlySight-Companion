package fr.hozakan.flysightcompanion.model

sealed interface ConfigFileState {
    data object Nothing : ConfigFileState {
        override fun toString(): String = "Nothing"
    }
    data object Loading : ConfigFileState {
        override fun toString(): String = "Loading"
    }
    data class Success(val config: ConfigFile) : ConfigFileState {
        override fun toString(): String = "Success"
    }
    data class Error(val message: String) : ConfigFileState {
        override fun toString(): String = "Error"
    }

    val conf: ConfigFile?
        get() = when (this) {
            is Success -> config
            else -> null
        }
}