package com.example.eid

import android.content.Context
import android.util.Log
import com.up2date.eidromania.eidromaniasdk.EIDRomaniaSDK

/**
 * Helper utility for displaying license status information.
 *
 * Usage in Activity:
 * ```
 * LicenseHelper.showLicenseStatus(this)
 * LicenseHelper.showLicenseFeatures()
 * ```
 */
object LicenseHelper {

    private const val TAG = "LicenseHelper"

    /**
     * Display license features and tier
     */
    fun showLicenseFeatures() {
        Log.i(TAG, "")
        Log.i(TAG, "═════════════════════════════════════════════════════════")
        Log.i(TAG, "          eID Romania SDK - License Features             ")
        Log.i(TAG, "═════════════════════════════════════════════════════════")
        Log.i(TAG, "")

        val licenseInfo = EIDRomaniaSDK.getLicenseInfo()
        if (licenseInfo != null) {
            Log.i(TAG, "  Issued To: ${licenseInfo.issuedTo}")
            Log.i(TAG, "  Issued At: ${licenseInfo.issuedAt}")
            Log.i(TAG, "  Expires At: ${licenseInfo.expiresAt}")
        } else {
            Log.w(TAG, "  ⚠️  No license information available")
        }

        Log.i(TAG, "")
        Log.i(TAG, "═════════════════════════════════════════════════════════")
        Log.i(TAG, "")
    }

    /**
     * Display detailed license status information in logcat.
     * Useful for debugging license issues.
     */
    fun showLicenseStatus(context: Context) {
        Log.i(TAG, "")
        Log.i(TAG, "═════════════════════════════════════════════════════════")
        Log.i(TAG, "          eID Romania SDK - License Status               ")
        Log.i(TAG, "═════════════════════════════════════════════════════════")
        Log.i(TAG, "")

        // Check if SDK is initialized
        val isInitialized = EIDRomaniaSDK.isInitialized()
        Log.i(TAG, "  SDK Initialized: ${if (isInitialized) "✓ Yes" else "✗ No"}")

        if (!isInitialized) {
            Log.w(TAG, "")
            Log.w(TAG, "  ⚠️  SDK not initialized!")
            Log.w(TAG, "     Call EIDRomaniaSDK.initialize() in Application.onCreate()")
            Log.w(TAG, "")
            Log.i(TAG, "═════════════════════════════════════════════════════════")
            Log.i(TAG, "")
            return
        }

        // Get license status
        when (val status = EIDRomaniaSDK.getLicenseStatus()) {
            is EIDRomaniaSDK.LicenseStatus.Active -> {
                Log.i(TAG, "  License Status: ✓ ACTIVE")
                Log.i(TAG, "  Expiry Date: ${status.expiryDate}")
                Log.i(TAG, "  Days Remaining: ${status.daysRemaining}")

                if (status.daysRemaining <= 7) {
                    Log.w(TAG, "")
                    Log.w(TAG, "  ⚠️  LICENSE EXPIRING SOON!")
                    Log.w(TAG, "     Contact vendor to renew your subscription")
                }
            }

            is EIDRomaniaSDK.LicenseStatus.Expired -> {
                Log.e(TAG, "  License Status: ✗ EXPIRED")
                Log.e(TAG, "  Expired On: ${status.expiredOn}")
                Log.e(TAG, "")
                Log.e(TAG, "  ❌ SDK will not work!")
                Log.e(TAG, "     Contact vendor to renew your license")
            }

            is EIDRomaniaSDK.LicenseStatus.Invalid -> {
                Log.e(TAG, "  License Status: ✗ INVALID")
                Log.e(TAG, "  Reason: ${status.reason}")
                Log.e(TAG, "")
                Log.e(TAG, "  Common causes:")

                when {
                    status.reason.contains("signature", ignoreCase = true) -> {
                        Log.e(TAG, "  - Invalid JWS signature")
                        Log.e(TAG, "  - License key corrupted or tampered")
                        Log.e(TAG, "  Solution: Check license key is correct and complete")
                    }
                    status.reason.contains("application", ignoreCase = true) ||
                    status.reason.contains("build", ignoreCase = true) -> {
                        Log.e(TAG, "  - License bound to different app or build")
                        Log.e(TAG, "  - Package name or app signature mismatch")
                        Log.e(TAG, "  Solution: Get new license for this app")
                    }
                    else -> {
                        Log.e(TAG, "  Solution: Contact vendor support")
                    }
                }
            }

            EIDRomaniaSDK.LicenseStatus.NotActivated -> {
                Log.e(TAG, "  License Status: ✗ NOT ACTIVATED")
                Log.e(TAG, "")
                Log.e(TAG, "  ❌ No license key provided!")
                Log.e(TAG, "     Provide license key in EIDRomaniaSDK.initialize()")
            }
        }

        Log.i(TAG, "")
        Log.i(TAG, "  Package Name: ${context.packageName}")
        Log.i(TAG, "")
        Log.i(TAG, "═════════════════════════════════════════════════════════")
        Log.i(TAG, "")
    }

    /**
     * Get a formatted summary of license status for UI display
     */
    fun getLicenseStatusSummary(): String {
        return when (val status = EIDRomaniaSDK.getLicenseStatus()) {
            is EIDRomaniaSDK.LicenseStatus.Active -> {
                if (status.daysRemaining <= 7) {
                    "⚠️ License expires in ${status.daysRemaining} days (${status.expiryDate})"
                } else {
                    "✓ License active until ${status.expiryDate}"
                }
            }
            is EIDRomaniaSDK.LicenseStatus.Expired -> {
                "✗ License expired on ${status.expiredOn}"
            }
            is EIDRomaniaSDK.LicenseStatus.Invalid -> {
                "✗ Invalid license: ${status.reason}"
            }
            EIDRomaniaSDK.LicenseStatus.NotActivated -> {
                "✗ SDK not activated"
            }
        }
    }

    /**
     * Check if license is valid and ready to use
     */
    fun isLicenseValid(): Boolean {
        return EIDRomaniaSDK.isInitialized() && EIDRomaniaSDK.isLicenseValid()
    }

    /**
     * Get days until license expires (-1 if invalid/expired)
     */
    fun getDaysRemaining(): Int {
        return when (val status = EIDRomaniaSDK.getLicenseStatus()) {
            is EIDRomaniaSDK.LicenseStatus.Active -> status.daysRemaining
            else -> -1
        }
    }
}
