package fr.hozakan.flysightcompanion.recordsmodule.business.analyze

import fr.hozakan.flysightcompanion.model.records.DataPoints

interface RecordParser {
    fun parse(fileLines: List<String>): DataPoints
}