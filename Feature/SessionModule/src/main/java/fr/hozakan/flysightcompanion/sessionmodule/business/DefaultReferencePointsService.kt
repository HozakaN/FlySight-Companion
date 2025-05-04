package fr.hozakan.flysightcompanion.sessionmodule.business

import android.content.Context
import fr.hozakan.flysightcompanion.dialogmodule.CreateReferencePointDialog
import fr.hozakan.flysightcompanion.dialogmodule.CreateReferencePointDialogResult
import fr.hozakan.flysightcompanion.dialogmodule.DialogResult
import fr.hozakan.flysightcompanion.dialogmodule.DialogService
import fr.hozakan.flysightcompanion.model.session.configuration.Coordinate
import fr.hozakan.flysightcompanion.model.session.configuration.ReferencePoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit
import kotlinx.coroutines.flow.update
import java.util.UUID

class DefaultReferencePointsService(
    context: Context,
    private val dialogService: DialogService
) : ReferencePointsService {

    private val sharedPrefs = context.getSharedPreferences("reference_points", Context.MODE_PRIVATE)

    private val _referencePoints = MutableStateFlow(emptyList<ReferencePoint>())
    override val referencePoints: StateFlow<List<ReferencePoint>> = _referencePoints.asStateFlow()

    init {
        loadReferencePoints()
    }

    override suspend fun createReferencePoint() {
        when (val result = dialogService.displayDialog(CreateReferencePointDialog())) {
            is CreateReferencePointDialogResult -> {
                addReferencePoint(result.referencePoint)
            }
            DialogResult.Dismiss -> {}
            else -> error("Create reference point result should not have another type (${result::class.java})")
        }
    }

    private fun loadReferencePoints() {
        sharedPrefs.getString("ref_points", "")?.let { refPointsStr ->
            var referencePoints = refPointsStr.split("\n").mapNotNull { refPointStr ->
                val split = refPointStr.split(";")
                if (split.size != 5) {
                    null
                } else {
                    ReferencePoint(
                        id = split[0],
                        name = split[1],
                        description = split[2],
                        coords = Coordinate(
                            latitude = split[3].toDoubleOrNull() ?: return@mapNotNull null,
                            longitude = split[4].toDoubleOrNull() ?: return@mapNotNull null
                        )
                    )
                }
            }
            if (referencePoints.isEmpty()) {
                referencePoints = listOf(
                    ReferencePoint(
                        id = UUID.randomUUID().toString(),
                        name = "Loudes target",
                        description = "Target at Loudes airfield",
                        coords = Coordinate(45.077200, 3.761141)
                    ),
                    ReferencePoint(
                        id = UUID.randomUUID().toString(),
                        name = "Beaufort NS4",
                        description = "Beaufort North South 4th point",
                        coords = Coordinate(34.733173, -76.668971)
                    )
                )
            }
            _referencePoints.value = referencePoints
        }
    }

    private fun addReferencePoint(referencePoint: ReferencePoint) {
        _referencePoints.update { referencePoints ->
            referencePoints + referencePoint
        }
        saveReferencePointList()
    }

    override suspend fun updateReferencePoint(referencePoint: ReferencePoint) {
        _referencePoints.update { referencePoints ->
            referencePoints.map { refPoint ->
                if (refPoint.id == referencePoint.id) {
                    referencePoint
                } else {
                    refPoint
                }
            }
        }
        saveReferencePointList()
    }

    override suspend fun deleteReferencePoint(referencePoint: ReferencePoint) {
        _referencePoints.update { referencePoints ->
            referencePoints.filter { it.id != referencePoint.id }
        }
        saveReferencePointList()
    }

    private fun saveReferencePointList() {
        val referencePointsStr = _referencePoints.value.joinToString(separator = "\n") { truc ->
            "${truc.id};${truc.name};${truc.description};${truc.coords.latitude};${truc.coords.longitude}"
        }
        sharedPrefs.edit {
            putString("ref_points", referencePointsStr)
        }
    }
}