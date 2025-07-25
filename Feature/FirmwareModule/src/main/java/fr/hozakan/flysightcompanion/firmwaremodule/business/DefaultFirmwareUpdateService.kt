package fr.hozakan.flysightcompanion.firmwaremodule.business

import android.content.Context
import com.google.gson.Gson
import fr.hozakan.flysightcompanion.model.firmware.FirmwareCompatibilityMatrix
import fr.hozakan.flysightcompanion.networkmodule.NetworkService
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DefaultFirmwareUpdateService(
    private val context: Context,
    private val gson: Gson,
    private val networkService: NetworkService
) : FirmwareUpdateService {

    private val _firmwareCompatibilityMatrix =
        MutableStateFlow(FirmwareCompatibilityMatrix.placeholder)
    override val firmwareCompatibilityMatrix: StateFlow<FirmwareCompatibilityMatrix> =
        _firmwareCompatibilityMatrix.asStateFlow()

    private val scope =
        CoroutineScope(SupervisorJob() + CoroutineName("FirmwareUpdateService") + Dispatchers.IO)

    init {
        // get the asset from the assets folder
        scope.launch {
            val matrix = networkService.getFirmwareCompatibilityMatrix()
                ?: loadFirmwareCompatibilityMatrixFromAssets()
            _firmwareCompatibilityMatrix.value = matrix
        }
    }

    private fun loadFirmwareCompatibilityMatrixFromAssets(): FirmwareCompatibilityMatrix {
        return try {
            val json = context.assets.open("firmware_compatibility_matrix.json").bufferedReader()
                .use { it.readText() }
            gson.fromJson(json, FirmwareCompatibilityMatrix::class.java)
        } catch (e: Exception) {
            FirmwareCompatibilityMatrix.placeholder
        }
    }

}