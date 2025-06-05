package fr.hozakan.flysightcompanion.model.ui

sealed class PlotBottomItem(val name: String) {
    data object Time : PlotBottomItem("Time")
    data object HorizontalDistance : PlotBottomItem("HorizontalDistance")
    data object TotalDistance : PlotBottomItem("TotalDistance")

    companion object {
        fun fromString(string: String): PlotBottomItem? {
            return when (string) {
                Time.name -> Time
                HorizontalDistance.name -> HorizontalDistance
                TotalDistance.name -> TotalDistance
                else -> null
            }
        }
    }
}