package fr.hozakan.flysightcompanion.recordsmodule.ui.plot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightcompanion.framework.tooling.triple
import fr.hozakan.flysightcompanion.model.records.dummyAnalyze
import fr.hozakan.flysightcompanion.model.records.dummyRecordFile
import fr.hozakan.flysightcompanion.model.ui.PlotBottomItem
import fr.hozakan.flysightcompanion.model.ui.PlotLeftItem
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import fr.hozakan.flysightcompanion.userpreferencesmodule.UserPrefService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlotSettingsViewModel @Inject constructor(
    private val userPrefService: UserPrefService
) : ViewModel() {

    private val _state = MutableStateFlow(PlotSettingsState())

    val state = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        userPrefService.plotDisplayPreferences
            .onEach { displayPreferences ->
                _state.update {
                    it.copy(
                        plotDisplayPreferences = displayPreferences
                    )
                }
            }
            .launchIn(viewModelScope)
    }

}