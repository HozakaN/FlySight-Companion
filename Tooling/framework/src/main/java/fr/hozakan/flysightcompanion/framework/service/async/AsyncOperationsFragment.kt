package fr.hozakan.flysightcompanion.framework.service.async

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AsyncOperationsFragment : Fragment() {

    private val pendingPermissionsJobs = mutableMapOf<Int, PermissionJob>()
    private val pendingResultsJobs =
        mutableMapOf<Int, CancellableContinuation<Pair<Int, Intent?>>>()
    private var requestCodeCounter = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (pendingPermissionsJobs.containsKey(requestCode)) {
            val job = pendingPermissionsJobs[requestCode]
            pendingPermissionsJobs.remove(requestCode)
            job?.let {
                it(grantResults.isNotEmpty() && grantResults.none { result -> result == -1 })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (pendingResultsJobs.containsKey(requestCode)) {
            val continuation = pendingResultsJobs[requestCode]
            pendingResultsJobs.remove(requestCode)
            continuation?.let {
                if (it.isActive) {
                    it.resume(resultCode to data)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermission(permissions: ArrayList<String>, callback: (Boolean) -> Unit) {
        val newIndex = requestCodeCounter++
        pendingPermissionsJobs[newIndex] = callback
        requestPermissions(permissions.toArray(arrayOf<String>()), newIndex)
    }

    suspend fun requestPermission(permission: String): Boolean = suspendCancellableCoroutine { continuation ->
        requestPermission(arrayListOf(permission)) {
            if (continuation.isActive) {
                continuation.resume(it)
            }
        }
    }

    suspend fun requestPermissions(vararg permissions: String): Boolean = suspendCancellableCoroutine { continuation ->
        requestPermission(arrayListOf(*permissions)) {
            if (continuation.isActive) {
                continuation.resume(it)
            }
        }
    }

    fun usePermissions(permissions: ArrayList<String>, job: (Boolean) -> Unit) {
        val notGrantedPermissions = mutableListOf<String>()
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    it
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notGrantedPermissions.add(it)
            }
        }
        if (notGrantedPermissions.isEmpty()) {
            job(true)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermission(permissions, job)
            } else {
                job(false)
            }
        }
    }

    suspend fun requestActivityResult(intent: Intent): Pair<Int, Intent?> =
        suspendCancellableCoroutine { continuation ->
            val newIndex = requestCodeCounter++
            pendingResultsJobs[newIndex] = continuation
            startActivityForResult(intent, newIndex)
            continuation.invokeOnCancellation {
                pendingResultsJobs.remove(newIndex)
            }
        }

}