package fr.hozakan.flysightcompanion.sessionmodule.ui.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fr.hozakan.flysightcompanion.model.session.configuration.SessionConfiguration
import fr.hozakan.flysightcompanion.model.session.configuration.SessionSource
import fr.hozakan.flysightcompanion.model.session.configuration.SessionType

@Composable
fun rememberSessionConfigurationForm(
    initialConfiguration: SessionConfiguration = SessionConfiguration.default()
): SessionConfigurationForm {
    return remember(initialConfiguration) {
        SessionConfigurationForm(initialConfiguration)
    }
}

@Stable
class SessionConfigurationForm(
    initialConfiguration: SessionConfiguration = SessionConfiguration.default()
) {

    internal var isDirty by mutableStateOf(false)
    internal var hasValidFileName by mutableStateOf(true)
    internal var isValid by mutableStateOf(true)

    internal var name by mutableStateOf<String?>(initialConfiguration.name)
    internal var description by mutableStateOf<String?>(initialConfiguration.description)

    fun updateSessionConfigurationName(fileName: String) {
        name = fileName
        isDirty = true
        checkValidity()
        hasValidFileName = fileName.isNotBlank()
    }

    fun updateConfigFileDescription(description: String) {
        this.description = description
        isDirty = true
        checkValidity()
    }

    private fun checkValidity() {
        isValid =
            name != null &&
                    description != null
    }

    fun toSessionConfiguration(): SessionConfiguration? {
        return SessionConfiguration(
            name = name ?: return null,
            description = description ?: return null,
            sessionType = SessionType.Visual
        )
    }
}