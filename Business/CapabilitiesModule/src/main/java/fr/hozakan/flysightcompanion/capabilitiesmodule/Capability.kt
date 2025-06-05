package fr.hozakan.flysightcompanion.capabilitiesmodule

sealed interface Capability {
    data object Bluetooth : Capability
}