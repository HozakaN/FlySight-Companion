package fr.hozakan.flysightcompanion.sessionmodule.business

import fr.hozakan.flysightcompanion.model.session.profile.ReferencePoint
import kotlinx.coroutines.flow.StateFlow

interface ReferencePointsService {
    val referencePoints: StateFlow<List<ReferencePoint>>
    suspend fun createReferencePoint()
    suspend fun updateReferencePoint(referencePoint: ReferencePoint)
    suspend fun deleteReferencePoint(referencePoint: ReferencePoint)
}