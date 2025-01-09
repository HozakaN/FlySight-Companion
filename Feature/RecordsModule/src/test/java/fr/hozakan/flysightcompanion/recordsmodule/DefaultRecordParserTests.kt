package fr.hozakan.flysightcompanion.recordsmodule

import fr.hozakan.flysightcompanion.recordsmodule.business.DefaultRecordParser
import org.junit.Assert
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")

class DefaultRecordParserTests {
    @Test
    fun `test nominal parsing`() {
        val parser = DefaultRecordParser()

        val fileContent = javaClass.classLoader
            ?.getResource("TRACK.CSV")?.readText()
        Assert.assertTrue(!fileContent.isNullOrBlank())
        if (fileContent == null) {
            Assert.fail("Could not read TRACK.CSV file from test resources")
            return
        }
        val fileLines = fileContent.lines()

        val record = parser.parse(fileLines)

        val localDateTime = LocalDateTime.parse("2024-12-18T22:52:16.400Z", dateTimeFormatter)

        Assert.assertEquals(7, record.size)
        Assert.assertEquals(localDateTime, record[0].dateTime)
    }
}