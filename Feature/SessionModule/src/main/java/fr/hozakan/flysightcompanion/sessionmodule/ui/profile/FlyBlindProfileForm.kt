package fr.hozakan.flysightcompanion.sessionmodule.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import fr.hozakan.flysightcompanion.model.session.FlyBlindConfiguration
import fr.hozakan.flysightcompanion.model.session.profile.Coordinate
import fr.hozakan.flysightcompanion.model.session.profile.ReferencePoint
import timber.log.Timber

@Composable
fun rememberFlyBlindProfileForm(
    initialConfiguration: FlyBlindConfiguration?
): FlyBlindProfileForm {
    return rememberSaveable(
        initialConfiguration,
        saver = FlyBlindProfileForm.Saver
    ) {
        FlyBlindProfileForm(
            initialConfiguration?.referencePoint,
            initialConfiguration?.isBellyFlying,
            initialConfiguration?.dzElev,
            initialConfiguration?.timeBetweenAudioUpdates,
            initialConfiguration?.keepMapOnExit
        )
    }
}

@Stable
class FlyBlindProfileForm(
    initialReferencePoint: ReferencePoint? = null,
    initialIsBellyFlying: Boolean? = null,
    initialDzElev: Int? = null,
    initialTimeBetweenAudioUpdates: Long? = null,
    initialKeepMapOnExit: Boolean? = null
) {
    companion object {
        val Saver: Saver<FlyBlindProfileForm, Any> = listSaver(
            save = { form ->
                val savedList = mutableListOf<Any>()
                savedList.add(form.isDirty)
                savedList.add(form.isValid)
                savedList.add(form.isBellyFlying)
                savedList.add(form.dzElev)
                savedList.add(form.timeBetweenAudioUpdates)
                savedList.add(form.keepMapOnExit)

                val refPoint = form.referencePoint
                if (refPoint != null) {
                    savedList.add(true) // ReferencePoint exists flag
                    savedList.add(refPoint.id)
                    savedList.add(refPoint.name)
                    savedList.add(refPoint.description)
                    savedList.add(refPoint.coords.latitude)
                    savedList.add(refPoint.coords.longitude)
                } else {
                    savedList.add(false) // No ReferencePoint
                }
                savedList
            },
            restore = { savedList ->
                try {
                    // Create a form with default settings first
                    val form = FlyBlindProfileForm()

                    // Then restore the saved state
                    var index = 0
                    form.isDirty = savedList[index++] as Boolean
                    form.isValid = savedList[index++] as Boolean
                    form.isBellyFlying = savedList[index++] as Boolean
                    form.dzElev = savedList[index++] as String
                    form.timeBetweenAudioUpdates = savedList[index++] as Long
                    form.keepMapOnExit = savedList[index++] as Boolean

                    val hasReferencePoint = savedList[index++] as Boolean
                    if (hasReferencePoint) {
                        val refId = savedList[index++] as String
                        val refName = savedList[index++] as String
                        val refDesc = savedList[index++] as String
                        val latitude = savedList[index++] as Double
                        val longitude = savedList[index++] as Double
                        val coords = Coordinate(latitude, longitude)
                        form.referencePoint = ReferencePoint(refId, refName, refDesc, coords)
                    } else {
                        form.referencePoint = null
                    }
                    // Ensure validity is re-checked after restoring
                    form.checkValidity()
                    form
                } catch (e: Exception) {
                    Timber.e(e, "Failed to restore FlyBlindProfileForm")
                    FlyBlindProfileForm() // Fallback to default
                }
            }
        )
    }

    internal var isDirty by mutableStateOf(false)
    internal var isValid by mutableStateOf(false)

    internal var referencePoint by mutableStateOf(initialReferencePoint)
    internal var isBellyFlying by mutableStateOf(
        initialIsBellyFlying ?: true
    )
    internal var dzElev by mutableStateOf("${initialDzElev ?: 0}")
    internal var keepMapOnExit by mutableStateOf(
        initialKeepMapOnExit ?: false
    )
    internal var timeBetweenAudioUpdates by mutableLongStateOf(
        initialTimeBetweenAudioUpdates ?: 10_000L
    )

    fun updateReferencePoint(referencePoint: ReferencePoint?) {
        this.referencePoint = referencePoint
        this.isDirty = true
        checkValidity()
    }

    fun updateIsBellyFlying(isBelly: Boolean) {
        this.isBellyFlying = isBelly
        this.isDirty = true
    }

    fun updateDzElev(elevation: String) {
        this.dzElev = elevation
        this.isDirty = true
        checkValidity()
    }
    
    fun updateKeepMapOnExit(keepMap: Boolean) {
        this.keepMapOnExit = keepMap
        this.isDirty = true
    }
    
    fun updateTimeBetweenAudioUpdates(timeMs: Long) {
        this.timeBetweenAudioUpdates = timeMs
        this.isDirty = true
    }

    internal fun checkValidity() {
        isValid = referencePoint != null && dzElev.toIntOrNull() != null && dzElev.toInt() >= 0
    }

    fun toFlyBlindConfiguration(): FlyBlindConfiguration? {
        checkValidity()
        if (!isValid) return null
        return referencePoint?.let {
            FlyBlindConfiguration(
                referencePoint = it,
                isBellyFlying = isBellyFlying,
                dzElev = dzElev.toInt(),
                timeBetweenAudioUpdates = timeBetweenAudioUpdates,
                keepMapOnExit = keepMapOnExit
            )
        }
    }
}
