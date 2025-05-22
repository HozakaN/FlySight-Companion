package fr.hozakan.flysightcompanion.locationmodule

import com.google.android.gms.common.api.ResolvableApiException

interface LocationCheckerActivity {
    suspend fun enableLocation(resolvableApiException: ResolvableApiException): Boolean
}
