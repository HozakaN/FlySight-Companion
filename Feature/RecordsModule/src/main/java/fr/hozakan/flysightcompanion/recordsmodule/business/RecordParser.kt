package fr.hozakan.flysightcompanion.recordsmodule.business

import fr.hozakan.flysightcompanion.model.records.DataPoints

interface RecordParser {
    fun parse(fileLines: List<String>): DataPoints
}