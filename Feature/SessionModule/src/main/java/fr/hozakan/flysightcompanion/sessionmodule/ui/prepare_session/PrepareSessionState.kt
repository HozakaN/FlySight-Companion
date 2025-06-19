package fr.hozakan.flysightcompanion.sessionmodule.ui.prepare_session

import androidx.compose.runtime.Stable
import fr.hozakan.flysightcompanion.framework.coroutine.flow.FlowEvent
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.locationmodule.LocationAvailabilityState
import fr.hozakan.flysightcompanion.model.session.FlyBlindConfiguration
import fr.hozakan.flysightcompanion.model.session.profile.ReferencePoint
import fr.hozakan.flysightcompanion.model.session.profile.SessionProfile
import fr.hozakan.flysightcompanion.model.session.profile.SessionSource
import fr.hozakan.flysightcompanion.model.session.profile.SessionSourceType
import fr.hozakan.flysightcompanion.model.session.profile.SessionType

@Stable
data class PrepareSessionState(
    val prepareSessionPhase: PrepareSessionPhase,
    val sessionProfiles: LoadingState<List<SessionProfile>>,
    val selectedProfile: SessionProfile?,
    val selectedSourceType: SessionSourceType,
    val selectedSource: SessionSource?,
    val availableSources: List<SessionSource>,
    val availableSessionTypes: List<SessionType>,
    val selectedSessionType: SessionType,
    val doneEvent: FlowEvent<Boolean>?,
    val locationAvailabilityState: LocationAvailabilityState = LocationAvailabilityState.ForegroundLocationNotAllowed,
    val referencePoints: List<ReferencePoint>,
    val flyBlindConfiguration: FlyBlindConfiguration?
)
