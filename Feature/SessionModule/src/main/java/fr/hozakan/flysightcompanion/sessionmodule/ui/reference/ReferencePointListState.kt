package fr.hozakan.flysightcompanion.sessionmodule.ui.reference

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightcompanion.model.session.profile.ReferencePoint

@Immutable
data class ReferencePointListState(
    val referencePoints: List<ReferencePoint>,
    val areReferencePointsSelectable: Boolean,
    val selectedReferencePoint: ReferencePoint?
)