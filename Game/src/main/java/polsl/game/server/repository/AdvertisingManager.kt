package polsl.game.server.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.ParcelUuid
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import polsl.game.spec.DeviceSpecifications.Companion.UUID_SERVICE_DEVICE
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

@SuppressLint("MissingPermission")
class AdvertisingManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
) {
    @SuppressLint("HardwareIds")
    val address: String? = bluetoothAdapter.address
    private val bluetoothLeAdvertiser: BluetoothLeAdvertiser by lazy {
        bluetoothAdapter.bluetoothLeAdvertiser
            ?: throw NullPointerException("Bluetooth nie uruchomiony")
    }
    private var advertisingCallback: AdvertiseCallback? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun startAdvertising() = suspendCancellableCoroutine { continuation ->
        advertisingCallback = object : AdvertiseCallback() {

            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                continuation.resume(Unit) { }
            }

            override fun onStartFailure(errorCode: Int) {
                continuation.resumeWithException(AdvertisingException(errorCode))
            }
        }

        continuation.invokeOnCancellation {
            bluetoothLeAdvertiser.stopAdvertising(advertisingCallback)
        }

        val advertisingSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .build()

        val advertisingData = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(UUID_SERVICE_DEVICE))
            .build()

        val scanResponse = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .build()

        bluetoothLeAdvertiser.startAdvertising(
            advertisingSettings,
            advertisingData,
            scanResponse,
            advertisingCallback
        )
    }

    fun stopAdvertising() {
        bluetoothLeAdvertiser.stopAdvertising(
            advertisingCallback
        )
    }
}