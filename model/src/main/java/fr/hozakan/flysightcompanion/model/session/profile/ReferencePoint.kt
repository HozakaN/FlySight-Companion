package fr.hozakan.flysightcompanion.model.session.profile

data class ReferencePoint(
    val id: String,
    val name: String,
    val description: String,
    val coords: Coordinate
)