package com.example.eid

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.up2date.eidromania.eidromaniasdk.EIDReaderConfig
import com.up2date.eidromania.eidromaniasdk.EIDReaderError
import com.up2date.eidromania.eidromaniasdk.EIDReaderLogger
import com.up2date.eidromania.eidromaniasdk.EIDRomaniaReader
import com.up2date.eidromania.eidromaniasdk.EIDRomaniaSDK
import com.up2date.eidromania.eidromaniasdk.NFCManager
import com.example.eid.ui.components.CardDataDisplay
import com.example.eid.ui.theme.MaterialTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.getValue

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private val viewModel: EIDReaderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                when {
                    !EIDRomaniaSDK.isInitialized() -> LicenseRequiredScreen()
                    nfcAdapter == null -> NoNfcSupportScreen()
                    nfcAdapter?.isEnabled == false -> NfcDisabledScreen()
                    else -> EIDReaderScreen(viewModel)
                }
            }
        }

        handleNfcIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent?) {
        if (intent?.action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            val tag: Tag? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            tag?.let { NFCManager.setTag(it) }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun LicenseRequiredScreen() {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("eID Romania SDK") })
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .padding(32.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Licență Necesară",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SDK-ul eID România necesită o licență validă pentru a funcționa.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "Pentru a obține o licență:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "1. Contactați Up2Date Software\n   Email: office@up2date.ro",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "2. Furnizați package name:\n   ${packageName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "3. Veți primi un token JWS\n   (ex: eyJhbGciOiJSUzI1NiIs...)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "4. Înlocuiți în EIDExampleApplication.kt:\n   LICENSE_KEY = \"token-primit\"",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "📋 Verificați logcat pentru detalii complete",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun NoNfcSupportScreen() {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("eID Romania") })
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "NFC nu este disponibil",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Acest dispozitiv nu suportă NFC. Pentru a citi carduri de identitate electronică, este necesar un dispozitiv cu suport NFC.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun NfcDisabledScreen() {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("eID Romania") })
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "NFC este dezactivat",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Te rugăm să activezi NFC din setările dispozitivului pentru a putea citi carduri de identitate electronică.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            startActivity(Intent(android.provider.Settings.ACTION_NFC_SETTINGS))
                        }
                    ) {
                        Text("Deschide Setări NFC")
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun EIDReaderScreen(viewModel: EIDReaderViewModel) {
        val uiState by viewModel.uiState.collectAsState()

        var can by remember { mutableStateOf("") }
        var pin by remember { mutableStateOf("") }
        var readFaceImage by remember { mutableStateOf(true) }
        var readSignature by remember { mutableStateOf(true) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("eID Romania") },
                    actions = {
                        if (uiState is EIDReaderUiState.Success) {
                            IconButton(onClick = { viewModel.reset() }) {
                                Icon(Icons.Default.Refresh, "Citire nouă")
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (val state = uiState) {
                    is EIDReaderUiState.Idle -> {
                        InputForm(
                            can = can,
                            onCanChange = { can = it },
                            pin = pin,
                            onPinChange = { pin = it },
                            readFaceImage = readFaceImage,
                            onReadFaceImageChange = { readFaceImage = it },
                            readSignature = readSignature,
                            onReadSignatureChange = { readSignature = it },
                            onReadClick = {
                                viewModel.startReading(can, pin, readFaceImage, readSignature)
                            }
                        )
                    }
                    is EIDReaderUiState.WaitingForCard -> {
                        WaitingForCardScreen(onCancel = { viewModel.cancelReading() })
                    }
                    is EIDReaderUiState.Reading -> {
                        ReadingScreen(
                            percentage = state.percentage,
                            message = state.message,
                            onCancel = { viewModel.cancelReading() }
                        )
                    }
                    is EIDReaderUiState.Success -> {
                        CardDataDisplay(card = state.card)
                    }
                    is EIDReaderUiState.Error -> {
                        ErrorScreen(
                            message = state.message,
                            onRetry = { viewModel.reset() }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun InputForm(
        can: String,
        onCanChange: (String) -> Unit,
        pin: String,
        onPinChange: (String) -> Unit,
        readFaceImage: Boolean,
        onReadFaceImageChange: (Boolean) -> Unit,
        readSignature: Boolean,
        onReadSignatureChange: (Boolean) -> Unit,
        onReadClick: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Citire Card Identitate",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Introduceți CAN și PIN-ul de pe cardul dumneavoastră:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = can,
                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) onCanChange(it) },
                label = { Text("CAN (6 cifre)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("Codul CAN se găsește pe spatele cardului") }
            )

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) onPinChange(it) },
                label = { Text("PIN (4 cifre)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("PIN-ul stabilit de dumneavoastră") }
            )

            HorizontalDivider()

            Text(
                text = "Opțiuni citire:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = readFaceImage, onCheckedChange = onReadFaceImageChange)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Citește fotografia")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = readSignature, onCheckedChange = onReadSignatureChange)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Citește semnătura")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onReadClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = can.length == 6 && pin.length == 4
            ) {
                Icon(Icons.Default.AccountBox, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Citește Card", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    @Composable
    private fun WaitingForCardScreen(onCancel: () -> Unit) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))

                Icon(
                    imageVector = Icons.Default.AccountBox,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Apropie cardul",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Mențineți cardul de identitate aproape de partea din spate a telefonului până la finalizarea citirii.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                OutlinedButton(onClick = onCancel) {
                    Text("Anulează")
                }
            }
        }
    }

    @Composable
    private fun ReadingScreen(
        percentage: Int,
        message: String,
        onCancel: () -> Unit
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 6.dp
                )

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Nu mișca cardul...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                OutlinedButton(onClick = onCancel) {
                    Text("Anulează")
                }
            }
        }
    }

    @Composable
    private fun ErrorScreen(
        message: String,
        onRetry: () -> Unit
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )

                Text(
                    text = "Eroare",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Încearcă din nou")
                }
            }
        }
    }
}
