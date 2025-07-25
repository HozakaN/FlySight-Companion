package fr.hozakan.flysightcompanion.sessionmodule.ui.reference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightcompanion.model.session.profile.ReferencePoint
import fr.hozakan.flysightcompanion.sessionmodule.business.ReferencePointsService
import fr.hozakan.flysightcompanion.sessionmodule.business.SessionProfilesService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReferencePointListViewModel @Inject constructor(
    private val sessionConfigurationService: SessionProfilesService,
    private val referencePointsService: ReferencePointsService
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            ReferencePointListState(
                referencePoints = emptyList(),
                areReferencePointsSelectable = true,
                selectedReferencePoint = null
            )
        )

    val state = _state.asStateFlow()

    init {
        referencePointsService.referencePoints
            .onEach { refPoints ->
                _state.value = _state.value.copy(
                    referencePoints = refPoints,
                    areReferencePointsSelectable = refPoints.isEmpty(),
                )
            }
            .launchIn(viewModelScope)
    }

    fun onReferencePointClicked(referencePoint: ReferencePoint) {
        val selectedPoint = if (_state.value.selectedReferencePoint == referencePoint) {
            null
        } else {
            referencePoint
        }
        _state.update {
            it.copy(
                selectedReferencePoint = selectedPoint
            )
        }
    }

    fun onReferencePointDelete(referencePoint: ReferencePoint) {
        viewModelScope.launch {
            referencePointsService.deleteReferencePoint(referencePoint)
        }
    }

    fun onCreateReferencePointClicked() {
        viewModelScope.launch {
            referencePointsService.createReferencePoint()
        }
    }

    fun onMapLongClick(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            referencePointsService.createReferencePointWithCoordinates(latitude, longitude)
        }
    }
}