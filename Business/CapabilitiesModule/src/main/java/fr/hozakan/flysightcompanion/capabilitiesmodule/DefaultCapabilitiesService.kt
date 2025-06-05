package fr.hozakan.flysightcompanion.capabilitiesmodule

class DefaultCapabilitiesService : CapabilitiesService {
    override suspend fun hasCapability(capability: Capability): Boolean {
        return false
    }
}