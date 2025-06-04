package fr.hozakan.flysightcompanion.sessionmodule.business

import fr.hozakan.flysightcompanion.model.session.profile.SessionProfile
import kotlinx.coroutines.flow.StateFlow

interface SessionProfilesService {
    val sessionProfiles: StateFlow<List<SessionProfile>>
    suspend fun saveProfile(sessionProfile: SessionProfile): SessionProfile
    suspend fun updateProfile(oldConf: SessionProfile, newConf: SessionProfile)
    suspend fun deleteProfile(sessionProfile: SessionProfile)
    suspend fun userPickProfile(): SessionProfile?
    suspend fun duplicateProfile(sessionProfile: SessionProfile)
}