package fr.hozakan.flysightcompanion.sessionmodule.ui.profile

import androidx.compose.runtime.Immutable
import com.qorvo.uwbtestapp.framework.coroutines.flow.FlowEvent
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.session.configuration.ReferencePoint
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile

@Immutable
data class SessionProfileState(
    val sessionProfile: SessionProfile,
    val configFiles: List<ConfigFile>,
    val referencePoints: List<ReferencePoint> = emptyList(),
    val configurationFound: Boolean = true,
    val fileSaved: FlowEvent<Boolean>? = null
)
