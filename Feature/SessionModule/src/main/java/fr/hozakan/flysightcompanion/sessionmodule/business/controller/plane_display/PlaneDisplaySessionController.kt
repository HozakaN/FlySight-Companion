package fr.hozakan.flysightcompanion.sessionmodule.business.controller.plane_display

import fr.hozakan.flysightcompanion.sessionmodule.business.controller.SessionController
import kotlinx.coroutines.flow.StateFlow

interface PlaneDisplaySessionController : SessionController {
    val elevation: StateFlow<Int>
    val dzElevation: StateFlow<Int>

    /**
     * 0 for performance, 1 for acrobatics
     */
    val discipline: StateFlow<Int>

    val colorBlindOption: StateFlow<Boolean>

    fun updateColorBlindOption(enabled: Boolean)

    fun updateDiscipline(discipline: Int)
    fun updateDzElev(dzElev:Int)

    companion object {
        const val ACRO_MIN_EXIT_HEIGHT = 3658 // meters
        const val ACRO_MAX_EXIT_HEIGHT = 3810 // meters
        const val PERF_MIN_EXIT_HEIGHT = 3200 // meters
        const val PERF_MAX_EXIT_HEIGHT = 3353 // meters
        val acroSliderRange = 3500..3968
        val perfSliderRange = 3100..3453

        fun truc() {
            val un = acroSliderRange.endInclusive
            val zero = acroSliderRange.start
            val firstStep = (ACRO_MIN_EXIT_HEIGHT - zero) / (un - zero)
            val secondStep = (ACRO_MAX_EXIT_HEIGHT - zero) / (un - zero)
        }
    }
}