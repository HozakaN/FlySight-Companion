package fr.hozakan.flysightcompanion.recordsmodule.business

import fr.hozakan.flysightcompanion.model.records.Record

interface RecordParser {
    fun parse(fileLines: List<String>): Record
}