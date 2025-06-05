package fr.hozakan.flysightcompanion.model.session

import fr.hozakan.flysightcompanion.model.session.profile.ReferencePoint

data class FlyBlindConfiguration(
    val referencePoint: ReferencePoint,
    val isBellyFlying: Boolean,
    val dzElev: Int,
    val timeBetweenAudioUpdates: Long, // ms. Negative to disable audio updates
    val keepMapOnExit: Boolean
)