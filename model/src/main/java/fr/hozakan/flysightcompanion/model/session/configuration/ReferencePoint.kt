package fr.hozakan.flysightcompanion.model.session.configuration

data class ReferencePoint(
    val id: String,
    val name: String,
    val description: String,
    val coords: Coordinate
)