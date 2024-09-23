package fr.hozakan.flysightble.configfilesmodule.ui.list_files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightble.configfilesmodule.business.ConfigFileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ListConfigFilesViewModel @Inject constructor(
    private val configFileService: ConfigFileService
) : ViewModel() {

    private val _state = MutableStateFlow(ListConfigFilesState())

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            configFileService.configFiles
                .collect { configFiles ->
                    _state.update {
                        it.copy(configFiles = configFiles)
                    }
                }
        }
    }

}