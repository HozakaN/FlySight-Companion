package fr.hozakan.flysightcompanion.model.session.configuration

data class DisplayItem(
    val displayableCapability: DisplayableCapability,
    val caseIndex: Int,
    val indexInCase: Int
)