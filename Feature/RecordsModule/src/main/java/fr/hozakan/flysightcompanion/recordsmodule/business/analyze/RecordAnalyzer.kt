package fr.hozakan.flysightcompanion.recordsmodule.business.analyze

import fr.hozakan.flysightcompanion.model.records.AnalyzeOptions
import fr.hozakan.flysightcompanion.model.records.DataPoints
import fr.hozakan.flysightcompanion.model.records.RecordAnalyze

interface RecordAnalyzer {
    suspend fun analyze(
        dataPoints: DataPoints,
        options: AnalyzeOptions = AnalyzeOptions()
    ): RecordAnalyze
}