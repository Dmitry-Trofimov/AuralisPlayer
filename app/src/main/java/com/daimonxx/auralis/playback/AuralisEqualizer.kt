package com.daimonxx.auralis.playback

/** UI profile is always represented by ten frequency points. Android's audio effect
 * maps these points to the bands actually provided by the device audio engine. */
data class EqualizerPreset(
    val id: String,
    val title: String,
    val valuesDb: List<Float>
)

object AuralisEqualizer {
    const val ACTION_SET_PROFILE = "com.daimonxx.auralis.action.SET_EQUALIZER_PROFILE"
    const val EXTRA_ENABLED = "enabled"
    const val EXTRA_GAINS = "gains_db"

    val frequenciesHz = listOf(31, 62, 125, 250, 500, 1_000, 2_000, 4_000, 8_000, 16_000)
    val frequencyLabels = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

    val presets = listOf(
        EqualizerPreset("flat", "Flat", listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)),
        EqualizerPreset("bass", "Bass", listOf(5f, 4.5f, 3.5f, 2f, 0.5f, 0f, 0f, 0f, 0f, 0f)),
        EqualizerPreset("ultra_bass", "Ultra Bass", listOf(8f, 7f, 5.5f, 3.5f, 1f, -0.5f, -1f, -1f, -0.5f, 0f)),
        EqualizerPreset("hip_hop", "Hip-Hop", listOf(5f, 4f, 2.5f, 0.5f, -1f, 0f, 1.5f, 2.5f, 3f, 2f)),
        EqualizerPreset("rock", "Rock", listOf(4.5f, 3.5f, 1.5f, -0.5f, -1.5f, 1f, 3f, 4f, 4f, 3f)),
        EqualizerPreset("jazz", "Jazz", listOf(3f, 2f, 1f, 1.5f, -0.5f, -0.5f, 1.5f, 2.5f, 3.5f, 3f)),
        EqualizerPreset("pop", "Pop", listOf(1.5f, 2f, 3f, 2f, 0f, 1f, 2f, 2.5f, 2f, 1.5f)),
        EqualizerPreset("classical", "Classical", listOf(0f, 1f, 2f, 1.5f, -0.5f, -1f, 0f, 1.5f, 2.5f, 3f)),
        EqualizerPreset("vocal", "Vocal", listOf(-1f, -1.5f, -1f, 1f, 3.5f, 4f, 3f, 1.5f, 0f, -1f)),
        EqualizerPreset("electronic", "Electronic", listOf(4f, 3f, 1.5f, 0f, -1f, 1f, 2f, 3.5f, 4f, 3f)),
        EqualizerPreset("acoustic", "Acoustic", listOf(0f, 1f, 2f, 2.5f, 1f, 0f, 1f, 2f, 2.5f, 2f))
    )

    val flatProfile = presets.first().valuesDb

    fun parseProfile(serialized: String?): List<Float> {
        val parsed = serialized
            ?.split(',')
            ?.mapNotNull { it.toFloatOrNull() }
            ?.take(frequenciesHz.size)
            .orEmpty()
        return if (parsed.size == frequenciesHz.size) parsed.map { it.coerceIn(-12f, 12f) } else flatProfile
    }

    fun serializeProfile(values: List<Float>): String =
        values.take(frequenciesHz.size).joinToString(",") { it.coerceIn(-12f, 12f).toString() }
}
