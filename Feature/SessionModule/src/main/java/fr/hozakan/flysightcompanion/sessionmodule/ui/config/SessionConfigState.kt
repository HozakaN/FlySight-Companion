package fr.hozakan.flysightcompanion.sessionmodule.ui.config

import androidx.compose.runtime.Immutable
import com.qorvo.uwbtestapp.framework.coroutines.flow.FlowEvent
import fr.hozakan.flysightcompanion.model.session.configuration.SessionConfiguration
import fr.hozakan.flysightcompanion.model.session.configuration.SessionSource
import fr.hozakan.flysightcompanion.model.session.configuration.StaticSessionSource

@Immutable
data class SessionConfigState(
    val sessionConfiguration: SessionConfiguration,
    val staticSessionSource: StaticSessionSource,
    val configurationFound: Boolean = true,
    val fileSaved: FlowEvent<Boolean>? = null
)