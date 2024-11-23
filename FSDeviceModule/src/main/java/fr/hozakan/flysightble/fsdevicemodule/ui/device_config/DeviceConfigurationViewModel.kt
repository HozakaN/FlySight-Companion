package fr.hozakan.flysightble.fsdevicemodule.ui.device_config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qorvo.uwbtestapp.framework.coroutines.flow.asEvent
import fr.hozakan.flysightble.configfilesmodule.business.DefaultConfigParser
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightble.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.FileState
import fr.hozakan.flysightble.model.FileInfo
import fr.hozakan.flysightble.model.defaultConfigFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeviceConfigurationViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(
        DeviceConfigurationState(
            configuration = defaultConfigFile()
        )
    )

    val state = _state.asStateFlow()

    fun loadConfiguration(conf: ConfigFile) {
        _state.update {
            it.copy(
                configuration = conf
            )
        }
    }

}