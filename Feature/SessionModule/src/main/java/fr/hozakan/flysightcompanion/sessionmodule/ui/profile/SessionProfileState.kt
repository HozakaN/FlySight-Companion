package fr.hozakan.flysightcompanion.sessionmodule.ui.profile

import androidx.compose.runtime.Immutable
import com.qorvo.uwbtestapp.framework.coroutines.flow.FlowEvent
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.session.profile.ReferencePoint
import fr.hozakan.flysightcompanion.model.session.profile.SessionProfile

@Immutable
data class SessionProfileState(
    val sessionProfile: SessionProfile,
    val configFiles: List<ConfigFile>,
    val referencePoints: List<ReferencePoint> = emptyList(),
    val profileFound: Boolean = true,
    val hasGpsFeature: Boolean = true,
    val hasGpsPermission: Boolean = false,
    val fileSaved: FlowEvent<Boolean>? = null
)
