package fr.hozakan.flysightcompanion.sessionmodule.ui.prepare_session

import androidx.compose.runtime.Stable
import com.qorvo.uwbtestapp.framework.coroutines.flow.FlowEvent
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import fr.hozakan.flysightcompanion.model.session.configuration.SessionSource
import fr.hozakan.flysightcompanion.model.session.configuration.SessionSourceType

@Stable
data class PrepareSessionState(
    val prepareSessionPhase: PrepareSessionPhase,
    val sessionProfiles: LoadingState<List<SessionProfile>>,
    val selectedProfile: SessionProfile?,
    val selectedSourceType: SessionSourceType,
    val selectedSource: SessionSource?,
    val availableSources: List<SessionSource>,
    val doneEvent: FlowEvent<Boolean>?
)

