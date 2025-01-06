package fr.hozakan.flysightcompanion.recordsmodule.business

import fr.hozakan.flysightcompanion.model.records.Record
import java.time.LocalDateTime

class DefaultRecordParser : RecordParser {
    override fun parse(fileLines: List<String>): Record {
        return Record(
            dateTime = LocalDateTime.now()
        )
    }
}