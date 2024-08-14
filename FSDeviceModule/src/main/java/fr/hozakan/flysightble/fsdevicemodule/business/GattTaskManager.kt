package fr.hozakan.flysightble.fsdevicemodule.business

import android.annotation.SuppressLint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class GattTaskManager {

    private val tasks = mutableListOf<GattTask>()
//    private val gattTasks = MutableSharedFlow<List<GattTask>>(replay = 0)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            while (true) {
                val task = tasks.firstOrNull()
                if (task != null) {
                    tasks.remove(task)
                    val gatt = task.gatt
                    val characteristic = task.characteristic
                    val command = task.command
                    val writeType = task.writeType
                    gatt.writeCharacteristic(
                        characteristic,
                        command,
                        writeType
                    )
                }
            }
        }
    }

    fun addTask(task: GattTask) {
        tasks += task
    }

}