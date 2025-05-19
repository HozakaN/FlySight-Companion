package fr.hozakan.flysightcompanion.model.session.configuration

data class DisplayItem(
    val displayableCapability: DisplayableCapability,
    val caseIndex: Int,
    val indexInCase: Int,
    val bag: DisplayItemBundle? = null
)

sealed interface DisplayItemBundle {
    data class DistanceToRefPointBundle(
        val referencePoint: ReferencePoint
    ) : DisplayItemBundle

    companion object {
        fun dstToRefPt(referencePoint: ReferencePoint): DistanceToRefPointBundle =
            DistanceToRefPointBundle(referencePoint)
    }
}