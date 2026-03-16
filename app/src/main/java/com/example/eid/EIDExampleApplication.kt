package com.example.eid

import android.app.Application
import android.util.Log
import com.up2date.eidromania.eidromaniasdk.EIDRomaniaSDK

/**
 * Application class that initializes the eID Romania SDK with license key.
 *
 * IMPORTANT: Replace LICENSE_KEY with your actual license key received from Up2Date.
 *
 * For production apps, consider storing the license key in:
 * - BuildConfig (gradle.properties)
 * - Remote config (Firebase Remote Config)
 * - Secure storage (EncryptedSharedPreferences)
 */
class EIDExampleApplication : Application() {

    companion object {
        private const val TAG = "EIDExample"

        /**
         * Your SDK license key (JWS/JWT format)
         *
         * ⚠️ PLACEHOLDER - Replace with your actual license key!
         *
         * To get your license key:
         * 1. Contact: office@up2date.ro
         * 2. Provide: Package name (com.voltfinance.voltapp)
         * 3. Receive: JWS token like: eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
         * 4. Replace the string below
         *
         * For production builds, use:
         * private val LICENSE_KEY = BuildConfig.EID_LICENSE_KEY
         */
        private const val LICENSE_KEY = "YOUR_LICENSE_KEY_HERE"

        /**
         * Set to true to enable debug logging
         */
        private const val DEBUG_MODE = true
    }

    override fun onCreate() {
        super.onCreate()

        // STEP 1: Initialize SDK with license key
        initializeSDK()

        // STEP 2: Check license status
        checkLicenseStatus()

        // STEP 3: Display detailed license info (optional)
        if (DEBUG_MODE) {
            LicenseHelper.showLicenseStatus(this)
            LicenseHelper.showLicenseFeatures()
        }
    }

    /**
     * Initialize the eID Romania SDK with license key
     */
    private fun initializeSDK() {
        try {
            Log.d(TAG, "Initializing eID Romania SDK...")

            EIDRomaniaSDK.initialize(
                context = this,
                licenseKey = LICENSE_KEY
            )

            Log.i(TAG, "✓ SDK initialized successfully")

        } catch (e: Exception) {
            Log.e(TAG, "═══════════════════════════════════════════════════════")
            Log.e(TAG, "  ❌ FAILED TO INITIALIZE eID Romania SDK")
            Log.e(TAG, "═══════════════════════════════════════════════════════")
            Log.e(TAG, "")
            Log.e(TAG, "Error: ${e.message}")
            Log.e(TAG, "")
            Log.e(TAG, "📝 TO FIX THIS:")
            Log.e(TAG, "")
            Log.e(TAG, "1. Contact Up2Date Software:")
            Log.e(TAG, "   Email: office@up2date.ro")
            Log.e(TAG, "")
            Log.e(TAG, "2. Request a license for:")
            Log.e(TAG, "   Package: ${packageName}")
            Log.e(TAG, "")
            Log.e(TAG, "3. You will receive a JWS token like:")
            Log.e(TAG, "   eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
            Log.e(TAG, "")
            Log.e(TAG, "4. Replace in EIDSampleApplication.kt:")
            Log.e(TAG, "   private const val LICENSE_KEY = \"your-token-here\"")
            Log.e(TAG, "")
            Log.e(TAG, "═══════════════════════════════════════════════════════")

            // In production, you might want to:
            // - Show a dialog to the user
            // - Disable eID reading features
            // - Report to analytics/crash reporting
        }
    }

    /**
     * Check and log license status (optional but recommended)
     */
    private fun checkLicenseStatus() {
        try {
            when (val status = EIDRomaniaSDK.getLicenseStatus()) {
                is EIDRomaniaSDK.LicenseStatus.Active -> {
                    Log.i(TAG, "✓ License is ACTIVE")
                    Log.i(TAG, "  Expires: ${status.expiryDate}")
                    Log.i(TAG, "  Days remaining: ${status.daysRemaining}")

                    // Show renewal reminder if expiring soon
                    if (status.daysRemaining <= 7) {
                        Log.w(TAG, "⚠️ License expires in ${status.daysRemaining} days!")
                        // TODO: Show in-app notification to renew
                    }
                }

                is EIDRomaniaSDK.LicenseStatus.Expired -> {
                    Log.e(TAG, "✗ License EXPIRED on ${status.expiredOn}")
                    // TODO: Show dialog prompting user to contact support
                    // TODO: Disable eID reading features
                }

                is EIDRomaniaSDK.LicenseStatus.Invalid -> {
                    Log.e(TAG, "✗ License INVALID: ${status.reason}")
                    // TODO: Show dialog with error message
                    // TODO: Disable eID reading features
                }

                EIDRomaniaSDK.LicenseStatus.NotActivated -> {
                    Log.e(TAG, "✗ SDK not activated")
                    // TODO: Show activation dialog
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to check license status", e)
        }
    }

    /**
     * Check if SDK is ready to use
     */
    fun isSDKReady(): Boolean {
        if (!EIDRomaniaSDK.isInitialized()) {
            Log.w(TAG, "SDK not initialized")
            return false
        }

        if (!EIDRomaniaSDK.isLicenseValid()) {
            Log.w(TAG, "Invalid or expired license")
            return false
        }

        return true
    }

    /**
     * Get license expiry information for UI display
     */
    fun getLicenseExpiryInfo(): LicenseExpiryInfo? {
        return when (val status = EIDRomaniaSDK.getLicenseStatus()) {
            is EIDRomaniaSDK.LicenseStatus.Active -> {
                LicenseExpiryInfo(
                    isValid = true,
                    expiryDate = status.expiryDate,
                    daysRemaining = status.daysRemaining,
                    isExpiringSoon = status.daysRemaining <= 7
                )
            }
            is EIDRomaniaSDK.LicenseStatus.Expired -> {
                LicenseExpiryInfo(
                    isValid = false,
                    expiryDate = status.expiredOn,
                    daysRemaining = 0,
                    isExpiringSoon = false
                )
            }
            else -> null
        }
    }

    /**
     * License expiry information for UI
     */
    data class LicenseExpiryInfo(
        val isValid: Boolean,
        val expiryDate: String,
        val daysRemaining: Int,
        val isExpiringSoon: Boolean
    )
}
