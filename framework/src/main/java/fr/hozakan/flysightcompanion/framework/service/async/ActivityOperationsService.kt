package fr.hozakan.flysightcompanion.framework.service.async

import android.content.Intent

/**
 * Start activity operations from the service layer
 */
interface ActivityOperationsService {
    fun usePermission(permission: String, job: (Boolean) -> Unit)
    fun usePermissions(permissions: List<String>, job: (Boolean) -> Unit)
    fun usePermissions(vararg permissions: String, job: (Boolean) -> Unit)
    suspend fun requestPermission(permission: String): Boolean
    suspend fun requestPermissions(vararg permissions: String): Boolean
    suspend fun requestActivityResult(intent: Intent): Pair<Int, Intent?>
}