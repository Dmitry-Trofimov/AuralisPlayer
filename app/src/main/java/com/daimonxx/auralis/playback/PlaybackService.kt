package com.daimonxx.auralis.playback

import android.app.PendingIntent
import android.content.Intent
import android.media.audiofx.Equalizer
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlin.math.ln
import kotlin.math.roundToInt

/** Owns playback and the audio effect independently of the Activity. */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var equalizer: Equalizer? = null
    private var equalizerEnabled = true
    private var equalizerGainsDb: List<Float> = AuralisEqualizer.flatProfile

    private val equalizerPlayerListener = object : Player.Listener {
        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            attachEqualizer(audioSessionId)
        }
    }

    override fun onCreate() {
        super.onCreate()

        val exoPlayer = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
            .apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    true
                )
                repeatMode = Player.REPEAT_MODE_OFF
                addListener(equalizerPlayerListener)
            }

        player = exoPlayer
        // MediaSession requires a non-null PendingIntent. Some launchers may return
        // null from getLaunchIntentForPackage(), so provide an explicit fallback.
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?: Intent(this, com.daimonxx.auralis.MainActivity::class.java)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val callback = object : MediaSession.Callback {
            override fun onConnectAsync(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): ListenableFuture<MediaSession.ConnectionResult> {
                val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                    .buildUpon()
                    .add(SessionCommand(AuralisEqualizer.ACTION_SET_PROFILE, Bundle.EMPTY))
                    .build()

                return Futures.immediateFuture(
                    MediaSession.ConnectionResult.AcceptedResultBuilder(session, controller)
                        .setAvailableSessionCommands(sessionCommands)
                        .build()
                )
            }

            override fun onCustomCommand(
                session: MediaSession,
                controller: MediaSession.ControllerInfo,
                customCommand: SessionCommand,
                args: Bundle
            ): ListenableFuture<SessionResult> {
                if (customCommand.customAction == AuralisEqualizer.ACTION_SET_PROFILE) {
                    val gains = args.getFloatArray(AuralisEqualizer.EXTRA_GAINS)
                    if (gains != null && gains.size == AuralisEqualizer.frequenciesHz.size) {
                        equalizerGainsDb = gains.map { it.coerceIn(-12f, 12f) }
                    }
                    equalizerEnabled = args.getBoolean(AuralisEqualizer.EXTRA_ENABLED, true)
                    applyEqualizerProfile()
                }
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
        }

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(sessionActivity)
            .setCallback(callback)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    private fun attachEqualizer(audioSessionId: Int) {
        releaseEqualizer()
        if (audioSessionId == C.AUDIO_SESSION_ID_UNSET || audioSessionId <= 0) return

        equalizer = runCatching {
            Equalizer(0, audioSessionId).apply {
                enabled = false
            }
        }.getOrNull()
        applyEqualizerProfile()
    }

    private fun applyEqualizerProfile() {
        val effect = equalizer ?: return
        runCatching {
            val bandCount = effect.numberOfBands.toInt()
            val range = effect.bandLevelRange
            for (bandIndex in 0 until bandCount) {
                val centerFrequencyHz = effect.getCenterFreq(bandIndex.toShort()) / 1000f
                val gainDb = interpolateGain(centerFrequencyHz, equalizerGainsDb)
                val milliBel = (gainDb * 100f).roundToInt().coerceIn(range[0].toInt(), range[1].toInt())
                effect.setBandLevel(bandIndex.toShort(), milliBel.toShort())
            }
            effect.enabled = equalizerEnabled
        }
    }

    /** Interpolates our 10-point profile at the centre frequencies supplied by Android. */
    private fun interpolateGain(frequencyHz: Float, values: List<Float>): Float {
        val frequencies = AuralisEqualizer.frequenciesHz
        if (frequencyHz <= frequencies.first()) return values.first()
        if (frequencyHz >= frequencies.last()) return values.last()

        val upperIndex = frequencies.indexOfFirst { it >= frequencyHz }.coerceAtLeast(1)
        val lowerIndex = upperIndex - 1
        val lowFrequency = frequencies[lowerIndex].toFloat()
        val highFrequency = frequencies[upperIndex].toFloat()
        val fraction = (ln(frequencyHz) - ln(lowFrequency)) / (ln(highFrequency) - ln(lowFrequency))
        return values[lowerIndex] + (values[upperIndex] - values[lowerIndex]) * fraction
    }

    private fun releaseEqualizer() {
        runCatching { equalizer?.release() }
        equalizer = null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val currentPlayer = player
        if (currentPlayer == null || !currentPlayer.playWhenReady || currentPlayer.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        player?.removeListener(equalizerPlayerListener)
        releaseEqualizer()
        mediaSession?.release()
        player?.release()
        player = null
        mediaSession = null
        super.onDestroy()
    }
}
