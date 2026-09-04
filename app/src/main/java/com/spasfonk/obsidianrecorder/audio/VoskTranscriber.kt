package com.spasfonk.obsidianrecorder.audio

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.StorageService
import java.io.IOException

data class TranscriptState(
    val finalizedText: String = "",
    val interimText: String = "",
    val isListening: Boolean = false,
    val lastError: String? = null
)

class VoskTranscriber(private val context: Context) {

    private val _state = MutableStateFlow(TranscriptState())
    val state: StateFlow<TranscriptState> = _state.asStateFlow()

    @Volatile
    private var recognizer: Recognizer? = null
    private var model: Model? = null
    @Volatile
    private var running = false
    private var recognizerThread: Thread? = null

    private val modelPath = "vosk-model-small-fr-0.22"

    fun start() {
        if (running) return
        _state.value = TranscriptState(isListening = false)

        recognizerThread = Thread {
            try {
                val fullModelDir = StorageService.unpack(context, modelPath, "models",
                    object : StorageService.Callback<Model> {
                        override fun onComplete(result: Model) {}
                        override fun onError(e: Exception) {}
                    })
                model = Model(fullModelDir.absolutePath)
                val rec = Recognizer(model, 44100.0f)
                rec.setWords(true)
                rec.setPartialWords(true)
                recognizer = rec
                running = true
                _state.value = _state.value.copy(isListening = true, lastError = null)
            } catch (e: IOException) {
                _state.value = _state.value.copy(
                    lastError = "Modèle Vosk introuvable dans assets/$modelPath. " +
                        "Téléchargez un modèle français sur alphacephei.com/vosk/models " +
                        "et placez-le dans app/src/main/assets/$modelPath/"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    lastError = "Erreur chargement Vosk : ${e.message}"
                )
            }
        }.apply {
            name = "VoskInitThread"
            start()
        }
    }

    fun acceptWaveForm(data: ShortArray, length: Int) {
        val rec = recognizer ?: return
        if (!running) return
        try {
            if (rec.acceptWaveForm(data, length)) {
                val result = rec.result
                if (result.isNotBlank()) {
                    val text = extractTextFromJson(result)
                    if (text.isNotBlank()) {
                        _state.value = _state.value.copy(
                            finalizedText = (_state.value.finalizedText + " " + text).trim()
                        )
                    }
                }
            }
            val partial = rec.partialResult
            if (partial.isNotBlank()) {
                val text = extractPartialFromJson(partial)
                _state.value = _state.value.copy(interimText = text)
            }
        } catch (_: Exception) { }
    }

    fun stop() {
        running = false
        try {
            recognizer?.let { rec ->
                runCatching {
                    val final = rec.finalResult
                    if (final.isNotBlank()) {
                        val text = extractTextFromJson(final)
                        if (text.isNotBlank()) {
                            _state.value = _state.value.copy(
                                finalizedText = (_state.value.finalizedText + " " + text).trim()
                            )
                        }
                    }
                }
                rec.close()
            }
        } catch (_: Exception) { }
        recognizer = null
        try { model?.close() } catch (_: Exception) { }
        model = null
        _state.value = _state.value.copy(isListening = false, interimText = "")
        recognizerThread?.join(2000)
        recognizerThread = null
    }

    private fun extractTextFromJson(json: String): String {
        return try {
            val textIdx = json.indexOf("\"text\"")
            if (textIdx < 0) return ""
            val colonIdx = json.indexOf(":", textIdx)
            if (colonIdx < 0) return ""
            val startQuote = json.indexOf("\"", colonIdx)
            if (startQuote < 0) return ""
            var endQuote = json.indexOf("\"", startQuote + 1)
            if (endQuote < 0) {
                endQuote = json.indexOf("\"\n", startQuote + 1)
                if (endQuote < 0) endQuote = json.indexOf("\"}", startQuote + 1)
                if (endQuote < 0) endQuote = json.indexOf("\",", startQuote + 1)
            }
            if (endQuote < 0 || endQuote <= startQuote) return ""
            json.substring(startQuote + 1, endQuote)
        } catch (_: Exception) {
            ""
        }
    }

    private fun extractPartialFromJson(json: String): String {
        return try {
            val textIdx = json.indexOf("\"partial\"")
            if (textIdx < 0) return ""
            val colonIdx = json.indexOf(":", textIdx)
            if (colonIdx < 0) return ""
            val startQuote = json.indexOf("\"", colonIdx)
            if (startQuote < 0) return ""
            var endQuote = json.indexOf("\"", startQuote + 1)
            if (endQuote < 0) {
                endQuote = json.indexOf("\"\n", startQuote + 1)
                if (endQuote < 0) endQuote = json.indexOf("\"}", startQuote + 1)
                if (endQuote < 0) endQuote = json.indexOf("\",", startQuote + 1)
            }
            if (endQuote < 0 || endQuote <= startQuote) return ""
            json.substring(startQuote + 1, endQuote)
        } catch (_: Exception) {
            ""
        }
    }
}