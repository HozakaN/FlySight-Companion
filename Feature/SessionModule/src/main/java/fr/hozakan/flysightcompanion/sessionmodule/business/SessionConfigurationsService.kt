package fr.hozakan.flysightcompanion.sessionmodule.business

import fr.hozakan.flysightcompanion.model.session.configuration.SessionConfiguration
import kotlinx.coroutines.flow.StateFlow

interface SessionConfigurationsService {
    val sessionConfigurations: StateFlow<List<SessionConfiguration>>
    suspend fun saveConfigFile(sessionConfiguration: SessionConfiguration): SessionConfiguration
    suspend fun updateSessionConfiguration(oldConf: SessionConfiguration, newConf: SessionConfiguration)
    suspend fun deleteSessionConfiguration(sessionConfiguration: SessionConfiguration)
    suspend fun userPickConfiguration(): SessionConfiguration?
    suspend fun duplicateSessionConfiguration(sessionConfiguration: SessionConfiguration)
}