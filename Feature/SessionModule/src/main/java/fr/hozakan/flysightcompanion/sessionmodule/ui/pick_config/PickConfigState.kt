package fr.hozakan.flysightcompanion.sessionmodule.ui.pick_config

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.session.configuration.SessionConfiguration

@Immutable
data class PickConfigState(
    val sessionConfigurations: LoadingState<List<SessionConfiguration>>
)