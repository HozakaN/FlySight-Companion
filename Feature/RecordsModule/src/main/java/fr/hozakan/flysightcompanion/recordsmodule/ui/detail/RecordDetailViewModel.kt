package fr.hozakan.flysightcompanion.recordsmodule.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightcompanion.model.records.dummyRecord
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class RecordDetailViewModel @Inject constructor(
    private val recordService: RecordService
) : ViewModel() {

    private val _state = MutableStateFlow(RecordDetailState())

    val state = _state.asStateFlow()

    private var loadJob: Job? = null

    fun loadRecord(recordName: String) {
        loadJob?.cancel()
        loadJob = recordService.records
            .map { it.filter { record -> record.phoneFilePath == recordName } }
            .map { it.firstOrNull() }
            .map { record ->
                record?.let {
                    it to recordService.loadRecordContent(it)
                }
            }
            .onEach { record ->
                _state.update {
                    it.copy(
                        record = record?.first ?: dummyRecord,
                        content = record?.second ?: ""
                    )
                }
            }
            .launchIn(viewModelScope)
    }

}