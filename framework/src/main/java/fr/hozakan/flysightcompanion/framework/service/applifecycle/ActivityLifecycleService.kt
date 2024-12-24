package fr.hozakan.flysightcompanion.framework.service.applifecycle

import androidx.activity.ComponentActivity
import fr.hozakan.flysightcompanion.framework.service.ListenableService
import kotlinx.coroutines.flow.Flow

interface ActivityLifecycleService : ListenableService<ComponentActivity?> {
    val currentActivity: ComponentActivity?
    val appInForeground: Flow<Boolean>
    suspend fun awaitActivity(): ComponentActivity
    suspend fun awaitNextResume()
}