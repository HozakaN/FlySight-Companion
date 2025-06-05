package fr.hozakan.flysightcompanion.loggermodule

import fr.hozakan.flysightcompanion.model.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultLoggerService : LoggerService {

    private val _logs = MutableStateFlow(emptyList<Log>())
    override val logs: StateFlow<List<Log>> = _logs.asStateFlow()

    override fun log(message: String) {
        _logs.value += Log(message)
    }
}