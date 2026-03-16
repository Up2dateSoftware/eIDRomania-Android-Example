package com.example.eid.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.up2date.eidromania.eidromaniasdk.data.RomanianElectronicIdentityCard
import java.text.SimpleDateFormat
import java.util.Locale
import com.up2date.eidromania.eidromaniasdk.AuthenticationResult


@Composable
fun CardDataDisplay(
    card: RomanianElectronicIdentityCard,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Date Carte Identitate",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Photo and basic info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Photo
                card.facialImageBase64?.let { base64 ->
                    val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Fotografie",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }

                // Basic info
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${card.surname} ${card.givenNames}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    DataField("CNP", card.cnp)
                    DataField("Serie și număr", card.documentNumber)
                }
            }
        }

        // Personal data
        InfoSection(title = "Date Personale") {
            DataField("Nume", card.surname)
            DataField("Prenume", card.givenNames)
            DataField("Sex", card.sex)
            DataField("Cetățenie", card.nationality)
            card.dateOfBirth?.let {
                DataField("Data nașterii", formatDate(it))
            }
            DataField("Locul nașterii", card.placeOfBirth)
        }

        // Document data
        InfoSection(title = "Date Document") {
            DataField("Serie și număr", card.documentNumber)
            DataField("Tip document", card.documentType)
            DataField("Emis de", card.issuingAuthority)
            card.dateOfIssue?.let {
                DataField("Data emiterii", formatDate(it))
            }
            card.dateOfExpiry?.let {
                DataField("Valabil până la", formatDate(it))
            }
        }

        // Address
        if (card.addressString.isNotBlank()) {
            InfoSection(title = "Adresă") {
                Text(
                    text = card.addressString,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Authentication Result
        card.authenticationResult?.let { authResult ->
            AuthenticationResultSection(authResult)
        }

        // Signature
        card.signatureImageBase64?.let { base64 ->
            InfoSection(title = "Semnătură") {
                val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Semnătură",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun DataField(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDate(date: java.util.Date): String {
    val format = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("ro-RO"))
    return format.format(date)
}

@Composable
private fun AuthenticationResultSection(
    authResult: AuthenticationResult
) {
    val (title, message, details, backgroundColor, textColor) = when (authResult) {
        is AuthenticationResult.Authentic -> {
            AuthInfo(
                title = "✓ Card Autentic",
                message = authResult.message,
                details = null,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        is AuthenticationResult.Failed -> {
            AuthInfo(
                title = "✗ Autentificare Eșuată",
                message = authResult.reason,
                details = authResult.details,
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                textColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }
        is AuthenticationResult.Warning -> {
            AuthInfo(
                title = "⚠ Avertisment",
                message = authResult.message,
                details = authResult.details,
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                textColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            HorizontalDivider(color = textColor.copy(alpha = 0.3f))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            details?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private data class AuthInfo(
    val title: String,
    val message: String,
    val details: String?,
    val backgroundColor: androidx.compose.ui.graphics.Color,
    val textColor: androidx.compose.ui.graphics.Color
)
