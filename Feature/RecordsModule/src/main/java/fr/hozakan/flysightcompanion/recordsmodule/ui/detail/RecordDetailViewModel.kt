package fr.hozakan.flysightcompanion.recordsmodule.ui.detail

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

class RecordDetailViewModel @Inject constructor(
    private val userPrefService: UserPrefService,
    private val recordService: RecordService
) : ViewModel() {

    private val _state = MutableStateFlow(RecordDetailState())

    val state = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        userPrefService.plotLeftItems
            .onEach { displayableItems ->
                _state.update {
                    it.copy(plotLeftItems = displayableItems)
                }
            }
            .launchIn(viewModelScope)
        userPrefService.plotBottomItem
            .onEach { displayableItem ->
                _state.update {
                    it.copy(plotBottomItem = displayableItem)
                }
            }
            .launchIn(viewModelScope)
        userPrefService.plotDisplayPreferences
            .onEach { displayPreferences ->
                _state.update {
                    it.copy(
                        plotDisplayPreferences = displayPreferences
                    )
                }
            }
            .launchIn(viewModelScope)
        userPrefService.unitSystem
            .onEach { unitSystem ->
                _state.update {
                    it.copy(unitSystem = unitSystem)
                }
            }
            .launchIn(viewModelScope)
    }

    fun loadRecord(recordName: String, softLoad: Boolean = false) {
        loadJob?.cancel()
        loadJob = recordService.records
            .map { it.filter { record -> record.phoneFilePath == recordName } }
            .map { it.firstOrNull() }
            .map { record ->
                record?.let {
                    it to recordService.loadRecordRawContent(it)
                }
            }.map { record ->
                if (!softLoad) {
                    record?.triple(recordService.analyzeRecord(record.first))
                } else {
                    record?.triple(dummyAnalyze)
                }
            }
            .onEach { record ->
                _state.update {
                    it.copy(
                        recordFile = record?.first ?: dummyRecordFile,
                        content = record?.second ?: "",
                        analyze = record?.third ?: dummyAnalyze
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun updatePlotLeftItems(leftItem: PlotLeftItem) {
        val newItems = state.value.plotLeftItems.toMutableList().also {
            if (it.contains(leftItem)) {
                it.remove(leftItem)
            } else {
                it.add(leftItem)
            }
        }
        viewModelScope.launch {
            userPrefService.updatePlotLeftItems(newItems)
        }
    }

    fun updatePlotBottomItems(bottomItem: PlotBottomItem) {
        if (bottomItem == state.value.plotBottomItem) return
        viewModelScope.launch {
            userPrefService.updatePlotBottomItem(bottomItem)
        }
    }

}