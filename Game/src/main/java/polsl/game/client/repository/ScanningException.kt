package polsl.game.client.repository

import android.bluetooth.le.ScanCallback

data class ScanningException(val errorCode: Int): Exception() {

    override val message: String?
        get() = when (errorCode) {
            ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> "Wyszukiwanie nie wpierane przez urządzenie"
            ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "Próba uruchomienia skanowania nieudana"
            ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> "Wewnątrzny błąd"
            ScanCallback.SCAN_FAILED_ALREADY_STARTED -> "Scanowanie już uruchomione"
            ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES -> "Zabrakło zasobów na urządzeniu"
            ScanCallback.SCAN_FAILED_SCANNING_TOO_FREQUENTLY -> "Zbyt często uruhamiane skanowanie"
            else -> super.message
        }
}