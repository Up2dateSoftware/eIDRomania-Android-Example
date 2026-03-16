package com.example.eid

import android.nfc.Tag
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.up2date.eidromania.eidromaniasdk.EIDReaderConfig
import com.up2date.eidromania.eidromaniasdk.EIDReaderError
import com.up2date.eidromania.eidromaniasdk.EIDReaderLogger
import com.up2date.eidromania.eidromaniasdk.EIDRomaniaReader
import com.up2date.eidromania.eidromaniasdk.NFCManager
import com.up2date.eidromania.eidromaniasdk.data.RomanianElectronicIdentityCard
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * ViewModel for managing eID card reading state and logic
 */
class EIDReaderViewModel : ViewModel() {

    private val reader = EIDRomaniaReader(
        EIDReaderConfig(
            logger = object : EIDReaderLogger {
                override fun log(level: EIDReaderLogger.Level, message: String, throwable: Throwable?) {
                    when (level) {
                        EIDReaderLogger.Level.DEBUG -> Log.d("EIDReader", message, throwable)
                        EIDReaderLogger.Level.INFO -> Log.i("EIDReader", message, throwable)
                        EIDReaderLogger.Level.WARNING -> Log.w("EIDReader", message, throwable)
                        EIDReaderLogger.Level.ERROR -> Log.e("EIDReader", message, throwable)
                    }
                }
            },
            maxRetries = 2,
            timeoutMillis = 20000,
            maxImageSizeBytes = 300 * 1024
        )
    )

    private val _uiState = MutableStateFlow<EIDReaderUiState>(EIDReaderUiState.Idle)
    val uiState: StateFlow<EIDReaderUiState> = _uiState.asStateFlow()

    private var readingJob: Job? = null

    /**
     * Start reading eID card
     */
    fun startReading(can: String, pin: String, readFaceImage: Boolean, readSignature: Boolean) {
        // Cancel previous job if any
        readingJob?.cancel()

        readingJob = viewModelScope.launch {
            try {
                // Validate input
                val validationError = validateInput(can, pin)
                if (validationError != null) {
                    _uiState.value = EIDReaderUiState.Error(validationError)
                    return@launch
                }

                // Wait for NFC tag
                _uiState.value = EIDReaderUiState.WaitingForCard
                NFCManager.startWaitingForTag()

                val tag = try {
                    withTimeout(30000) {
                        NFCManager.currentTag.first { it != null }
                    }
                } catch (e: Exception) {
                    _uiState.value = EIDReaderUiState.Error("Timeout așteptând card. Te rog apropie cardul.")
                    return@launch
                } finally {
                    NFCManager.stopWaitingForTag()
                }

                if (tag == null) {
                    _uiState.value = EIDReaderUiState.Error("Nu s-a detectat niciun card NFC.")
                    return@launch
                }

                // Start reading with progress tracking
                _uiState.value = EIDReaderUiState.Reading(0, "Pornire citire...")

                // Collect progress updates
                val progressJob = launch {
                    reader.readingProgress.collectLatest { progress ->
                        _uiState.value = EIDReaderUiState.Reading(
                            progress.percentage,
                            progress.description
                        )
                    }
                }

                // Perform read
                val result = reader.read(
                    nfcTag = tag,
                    can = can.trim(),
                    pin = pin.trim(),
                    readProfileImage = readFaceImage,
                    readSignatureImage = readSignature
                )

                progressJob.cancel()

                // Handle result
                result.fold(
                    onSuccess = { card ->
                        _uiState.value = EIDReaderUiState.Success(card)
                    },
                    onFailure = { error ->
                        val userMessage = when (error) {
                            is EIDReaderError.InvalidPIN -> {
                                if (error.attemptsRemaining != null) {
                                    "PIN incorect. Mai aveți ${error.attemptsRemaining} încercări."
                                } else {
                                    "PIN incorect. Verificați codul PIN de 4 cifre."
                                }
                            }
                            is EIDReaderError.InvalidCAN -> {
                                "CAN incorect. Verificați codul CAN de 6 cifre de pe card."
                            }
                            is EIDReaderError.TagLost -> {
                                "Conexiune pierdută cu cardul. Mențineți cardul aproape și încercați din nou."
                            }
                            is EIDReaderError.CardLocked -> {
                                "Card blocat din cauza PIN-urilor incorecte. Contactați o autoritate de emitere."
                            }
                            is EIDReaderError.Timeout -> {
                                "Timeout la citirea cardului. Încercați din nou."
                            }
                            is EIDReaderError.InvalidInput -> {
                                error.message ?: "Date de intrare invalide."
                            }
                            else -> {
                                "Eroare: ${error.message ?: "Eroare necunoscută"}"
                            }
                        }
                        _uiState.value = EIDReaderUiState.Error(userMessage)
                    }
                )
            } catch (e: Exception) {
                Log.e("EIDReader", "Unexpected error", e)
                _uiState.value = EIDReaderUiState.Error("Eroare neașteptată: ${e.message}")
            } finally {
                NFCManager.stopWaitingForTag()
                NFCManager.clearTag()
            }
        }
    }

    /**
     * Cancel ongoing reading operation
     */
    fun cancelReading() {
        readingJob?.cancel()
        readingJob = null
        NFCManager.stopWaitingForTag()
        NFCManager.clearTag()
        _uiState.value = EIDReaderUiState.Idle
    }

    /**
     * Reset to idle state
     */
    fun reset() {
        _uiState.value = EIDReaderUiState.Idle
    }

    private fun validateInput(can: String, pin: String): String? {
        val cleanCan = can.trim()
        val cleanPin = pin.trim()

        if (cleanCan.length != 6) {
            return "CAN trebuie să aibă exact 6 cifre."
        }
        if (!cleanCan.all { it.isDigit() }) {
            return "CAN trebuie să conțină doar cifre."
        }
        if (cleanPin.length != 4) {
            return "PIN trebuie să aibă exact 4 cifre."
        }
        if (!cleanPin.all { it.isDigit() }) {
            return "PIN trebuie să conțină doar cifre."
        }
        return null
    }

    override fun onCleared() {
        super.onCleared()
        cancelReading()
    }
}

/**
 * UI state for eID reader screen
 */
sealed class EIDReaderUiState {
    object Idle : EIDReaderUiState()
    object WaitingForCard : EIDReaderUiState()
    data class Reading(val percentage: Int, val message: String) : EIDReaderUiState()
    data class Success(val card: RomanianElectronicIdentityCard) : EIDReaderUiState()
    data class Error(val message: String) : EIDReaderUiState()
}
