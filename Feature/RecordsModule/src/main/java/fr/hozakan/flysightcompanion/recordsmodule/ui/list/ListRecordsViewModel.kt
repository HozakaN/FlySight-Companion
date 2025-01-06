package fr.hozakan.flysightcompanion.recordsmodule.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightcompanion.model.records.Record
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class ListRecordsViewModel @Inject constructor(
    private val recordService: RecordService
) : ViewModel() {

    private val _state = MutableStateFlow(ListRecordsState())

    val state = _state.asStateFlow()

    init {
        recordService.records
            .onEach {
                _state.value = _state.value.copy(records = it)
            }
            .launchIn(viewModelScope)
    }

    fun deleteRecord(record: Record) {
        viewModelScope.launch {
            recordService.deleteRecord(record)
        }
    }

}