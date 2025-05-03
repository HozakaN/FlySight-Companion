package fr.hozakan.flysightcompanion.sessionmodule.ui.reference

import androidx.lifecycle.ViewModel
import fr.hozakan.flysightcompanion.model.session.configuration.ReferencePoint
import fr.hozakan.flysightcompanion.sessionmodule.business.SessionProfilesService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class ReferencePointListViewModel @Inject constructor(
    private val sessionConfigurationService: SessionProfilesService
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            ReferencePointListState(
                referencePoints = emptyList(),
                areReferencePointsSelectable = true
            )
        )

    val state = _state.asStateFlow()

    init {
    }

    fun onReferencePointClicked(referencePoint: ReferencePoint) {}
    fun onReferencePointDelete(referencePoint: ReferencePoint) {}
}