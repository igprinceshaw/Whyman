package com.example.game

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.*

class RetroMusicPlayer {
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var job: Job? = null

    fun start(scope: CoroutineScope) {
        if (isPlaying) return
        isPlaying = true
        
        job = scope.launch(Dispatchers.Default) {
            val sampleRate = 8000
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_8BIT
            )
            
            try {
                audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_8BIT,
                    minBufferSize.coerceAtLeast(1024),
                    AudioTrack.MODE_STREAM
                )
                
                audioTrack?.play()
                
                // Beautiful retro looping space melody
                val notes = doubleArrayOf(
                    146.83, 146.83, 164.81, 196.00, // D3, D3, E3, G3
                    146.83, 146.83, 220.00, 196.00, // D3, D3, A3, G3
                    146.83, 146.83, 164.81, 196.00,
                    293.66, 293.66, 261.63, 220.00  // D4, D4, C4, A3
                )
                
                var noteIdx = 0
                val bufferSize = 800 // 100ms blocks
                val buffer = ByteArray(bufferSize)
                
                while (isPlaying && isActive) {
                    val freq = notes[noteIdx]
                    // Generate 8-bit square wave with moderate duration
                    val durationMs = 200
                    val samples = (sampleRate * durationMs / 1000)
                    
                    var sampleCount = 0
                    while (sampleCount < samples && isPlaying && isActive) {
                        val chunk = minOf(bufferSize, samples - sampleCount)
                        for (i in 0 until chunk) {
                            val t = (sampleCount + i).toDouble() / sampleRate
                            // Square wave formula with low volume (amplitude of 8)
                            val value = if (Math.sin(2 * Math.PI * freq * t) > 0) 8 else -8
                            buffer[i] = (value + 128).toByte() // PCM 8-bit unsigned
                        }
                        audioTrack?.write(buffer, 0, chunk)
                        sampleCount += chunk
                    }
                    
                    // Brief pause between notes
                    val silenceSamples = (sampleRate * 25 / 1000)
                    for (i in 0 until silenceSamples.coerceAtMost(bufferSize)) {
                        buffer[i] = 128.toByte()
                    }
                    audioTrack?.write(buffer, 0, silenceSamples.coerceAtMost(bufferSize))
                    
                    noteIdx = (noteIdx + 1) % notes.size
                    delay(5)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stopInternal()
            }
        }
    }

    fun stop() {
        isPlaying = false
        job?.cancel()
        job = null
        stopInternal()
    }

    private fun stopInternal() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignore
        }
        audioTrack = null
    }
}
