package fr.hozakan.flysightcompanion.capabilitiesmodule

interface CapabilitiesService {
    suspend fun hasCapability(capability: Capability): Boolean
}