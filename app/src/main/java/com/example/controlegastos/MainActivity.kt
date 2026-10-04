package com.example.controlegastos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.controlegastos.ui.ControleGastosApp
import com.example.controlegastos.ui.intro.IntroScreen
import com.example.controlegastos.ui.theme.ControleGastosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            var mostrarIntro by rememberSaveable {
                mutableStateOf(true)
            }

            val solicitadorPermissaoNotificacao =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) {
                    // O app segue funcionando mesmo se a permissão for negada.
                }

            LaunchedEffect(Unit) {
                val precisaSolicitarPermissao =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED

                if (precisaSolicitarPermissao) {
                    solicitadorPermissaoNotificacao.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }

            ControleGastosTheme {
                if (mostrarIntro) {
                    IntroScreen(
                        onIntroFinalizada = {
                            mostrarIntro = false
                        }
                    )
                } else {
                    ControleGastosApp()
                }
            }
        }
    }
}