package fr.hozakan.flysightcompanion.loggermodule

import fr.hozakan.flysightcompanion.model.Log
import kotlinx.coroutines.flow.StateFlow

interface LoggerService {
    val logs: StateFlow<List<Log>>
    fun log(message: String)
}