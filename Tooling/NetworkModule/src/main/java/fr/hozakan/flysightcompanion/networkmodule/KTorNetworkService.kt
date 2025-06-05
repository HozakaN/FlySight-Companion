package fr.hozakan.flysightcompanion.networkmodule

import android.content.Context
import com.google.gson.Gson
import fr.hozakan.flysightcompanion.model.firmware.FirmwareCompatibilityMatrix
import fr.hozakan.flysightcompanion.model.firmware.FirmwareInfo
import fr.hozakan.flysightcompanion.model.firmware.FirmwareVersion
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.UnknownHostException

class KTorNetworkService(
    private val context: Context
) : NetworkService {

    private val client = HttpClient(OkHttp) {
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    Timber.d("[KTOR]: $message")
                }

            }
        }
        install(HttpRedirect)
    }

    private val _firmwares = MutableStateFlow<List<FirmwareVersion>>(emptyList())
    override val firmwares: StateFlow<List<FirmwareVersion>> = _firmwares.asStateFlow()

    private val _firmwareCompatibilityMatrix = MutableStateFlow(FirmwareCompatibilityMatrix.placeholder)
    override val firmwareCompatibilityMatrix: StateFlow<FirmwareCompatibilityMatrix> = _firmwareCompatibilityMatrix.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            _firmwares.value = getAvailableFirmwares()
            _firmwareCompatibilityMatrix.value = getFirmwareCompatibilityMatrix()
        }
    }

    private suspend fun getAvailableFirmwares(): List<FirmwareVersion> {
        return try {
            client.get {
                url(githubTagPagesUrl)
            }
        } catch (ex: Exception) {
            Timber.i(ex)
            null
        }?.let { response ->
            val result: List<FirmwareVersion> = if (response.status == HttpStatusCode.OK) {
                val content = response.readRawBytes().toString(Charsets.UTF_8)
                val availableFirmwares = parseFirmwarePage(content)
                availableFirmwares
            } else {
                emptyList()
            }
            result
        } ?: emptyList()
    }

    private suspend fun getFirmwareCompatibilityMatrix(): FirmwareCompatibilityMatrix {
        return try {
            client.get {
                url(githubCompatibilityMatrixUrl)
            }
        } catch (ex: Exception) {
            Timber.i(ex)
            null
        }?.let { response ->
            val result: FirmwareCompatibilityMatrix = if (response.status == HttpStatusCode.OK) {
                val content = response.readRawBytes().toString(Charsets.UTF_8)
                parseCompatibilityMatrix(content)
            } else {
                FirmwareCompatibilityMatrix.placeholder
            }
            result
        } ?: FirmwareCompatibilityMatrix.placeholder
    }

    private fun parseFirmwarePage(content: String): List<FirmwareVersion> {
        //https://flysight.ca/wp-admin/admin-post.php?action=download_firmware&amp;firmware_file=B3_v2023.09.22.sfb
        return content.split("\n")
            .filter { it.contains(githubTagMarker) }
            .map { line ->
                FirmwareVersion(
                    versionName = line.substring(line.indexOf(githubTagMarker) + githubTagMarker.length)
                        .let { it.substring(0, it.indexOf("data-view-component") -2) }
                )
            }
    }

    private fun parseCompatibilityMatrix(content: String): FirmwareCompatibilityMatrix {
        val json = Gson()
        return try {
            json.fromJson(content, FirmwareCompatibilityMatrix::class.java)
        } catch (e: Exception) {
            Timber.e(e)
            FirmwareCompatibilityMatrix.placeholder
        }
    }

    override suspend fun downloadFirmware(deviceBatch: String, firmwareInfo: FirmwareInfo): ByteArray? {
        val response = client.get("$flySightFirmwareDownloadSite${deviceBatch}_${firmwareInfo.name}.sfb")
        if (response.status != HttpStatusCode.OK) {
            return null
        }
        return response.bodyAsBytes()
    }
}

private const val flySightUpdateUrl = "https://flysight.ca/firmware"
private const val flySightFirmwareDownloadSite =
    "https://flysight.ca/wp-admin/admin-post.php?action=download_firmware&firmware_file="

private const val githubTagPagesUrl = "https://github.com/flysight/flysight-2-firmware/tags"
private const val githubTagMarker = "<a href=\"/flysight/flysight-2-firmware/releases/tag/"

private const val githubCompatibilityMatrixUrl = "https://hozakan.github.io/FlySight-Companion/firmware_compatibility_matrix.json"