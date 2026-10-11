package com.voiceflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

// MARK: - VoiceFlow
//
// Voice messages, reimagined for Telegram.
// A design concept + ready-to-implement prototype by @ograinhard.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatScreen()
        }
    }
}
