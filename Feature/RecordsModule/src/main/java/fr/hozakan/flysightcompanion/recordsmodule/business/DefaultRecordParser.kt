package fr.hozakan.flysightcompanion.recordsmodule.business

import fr.hozakan.flysightcompanion.model.records.DataPoint
import fr.hozakan.flysightcompanion.model.records.DataPoints
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DefaultRecordParser : RecordParser {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")

    override fun parse(fileLines: List<String>): DataPoints {
        return fileLines.mapNotNull { line ->
            val parts = line.split(",")
            if (parts[0] == "\$GNSS") {
                DataPoint(
                    dateTime = LocalDateTime.parse(parts[1], dateTimeFormatter),
                    hasGeodetic = true,
                    latitude = parts[2].toDouble(),
                    longitude = parts[3].toDouble(),
                    hMSL = parts[4].toDouble(),
                    velN = parts[5].toDouble(),
                    velE = parts[6].toDouble(),
                    velD = parts[7].toDouble(),
                    hAcc = parts[8].toDouble(),
                    vAcc = parts[9].toDouble(),
                    sAcc = parts[10].toDouble(),
                    numSV = parts[11].toInt()
                )
            } else {
                null
            }
        }
    }
}