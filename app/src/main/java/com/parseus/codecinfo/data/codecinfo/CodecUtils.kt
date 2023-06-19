package com.parseus.codecinfo.data.codecinfo

import android.annotation.SuppressLint
import android.content.Context
import android.media.CamcorderProfile
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.CodecCapabilities.*
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.util.Range
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import androidx.preference.PreferenceManager
import com.parseus.codecinfo.*
import com.parseus.codecinfo.data.DetailsProperty
import com.parseus.codecinfo.data.codecinfo.colorformats.*
import com.parseus.codecinfo.data.codecinfo.profilelevels.*
import com.parseus.codecinfo.data.codecinfo.profilelevels.VP9Levels.*
import com.parseus.codecinfo.utils.*
import java.util.*
import kotlin.math.min

// Source:
// https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android10-release/media/java/android/media/MediaCodecInfo.java#1052
private const val DEFAULT_MAX_INPUT_CHANNEL_LIMIT = 30
private const val DEFAULT_MAX_FRAME_RATE = 960
private const val DEFAULT_MAX_SIZE = 32768

// Source:
// https://web.archive.org/web/20170503180053/http://www.divx.com/files/DivX-Profiles_Tech-Specs.pdf
// Assume the best possible resolution and framerate, if unknown.
private const val DIVX4_480P_MAX_FRAME_RATE = 30
private const val DIVX4_576P_MAX_FRAME_RATE = 25
private const val DIVX6_720P_MAX_FRAME_RATE = 60
private const val DIVX6_1080P_MAX_FRAME_RATE = 30
private val DIVX4_MAX_RESOLUTION = intArrayOf(720, 576)
private val DIVX6_MAX_RESOLUTION = intArrayOf(1920, 1080)

// TODO: Find a good official source of the spec.
private const val AC3_MAX_SAMPLE_RATE = 48000

private const val GOOGLE_RAW_DECODER = "OMX.google.raw.decoder"
private const val MEDIATEK_RAW_DECODER = "OMX.MTK.AUDIO.DECODER.RAW"

private val platformSupportedTypes = arrayOf(
        "audio/3gpp",
        "audio/amr-mb",
        "audio/amr-wb",
        "audio/flac",
        "audio/g711-alaw",
        "audio/g711-mlaw",
        "audio/gsm",
        "audio/mp4a-latm",
        "audio/mpeg",
        "audio/opus",
        "audio/raw",
        "audio/vorbis")

private val framerateResolutions = arrayOf(
        intArrayOf(176, 144), intArrayOf(256, 144),
        intArrayOf(320, 240), intArrayOf(426, 240),
        intArrayOf(480, 360), intArrayOf(640, 360),
        intArrayOf(640, 480), intArrayOf(854, 480),
        intArrayOf(720, 576),
        intArrayOf(1280, 720),
        intArrayOf(1920, 1080),
        intArrayOf(3840, 2160),
        intArrayOf(7680, 4320)
)
private val framerateClasses = arrayOf(
        "144p", "144p (YouTube)",
        "240p", "240p (widescreen)",
        "360p", "360p (widescreen)",
        "480p", "480p (widescreen)",
        "576p",
        "720p",
        "1080p",
        "4K",
        "8K"
)

private val knownVendorLowLatencyOptions = listOf(
    // https://cs.android.com/android/platform/superproject/+/master:hardware/qcom/sdm845/media/mm-video-v4l2/vidc/vdec/src/omx_vdec_extensions.hpp
    "vendor.qti-ext-dec-low-latency.enable",
    // https://developer.huawei.com/consumer/cn/forum/topic/0202325564295980115
    "vendor.hisi-ext-low-latency-video-dec.video-scene-for-low-latency-req",
    "vendor.rtc-ext-dec-low-latency.enable",
    // https://github.com/codewalkerster/android_vendor_amlogic_common_prebuilt_libstagefrighthw/commit/41fefc4e035c476d58491324a5fe7666bfc2989e
    "vendor.low-latency.enable"
)

private var mediaCodecInfos: Array<MediaCodecInfo> = emptyArray()

val audioCodecList: MutableList<CodecSimpleInfo> = mutableListOf()
val videoCodecList: MutableList<CodecSimpleInfo> = mutableListOf()


fun getSimpleCodecInfoList(context: Context, isAudio: Boolean): MutableList<CodecSimpleInfo> {
    if (isAudio && audioCodecList.isNotEmpty()) {
        return audioCodecList
    } else if (!isAudio && videoCodecList.isNotEmpty()) {
        return videoCodecList
    }

    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    if (mediaCodecInfos.isEmpty()) {
        mediaCodecInfos = if (SDK_INT >= 21) {
            try {
                MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos
            } catch (e: Exception) {
                // Some devices (like Xiaomi Redmi Note 4) seem to
                // throw an exception when trying to list codecs.
                // Return an empty list to inform the user abput it.
                return mutableListOf()
            }
        } else {
            try {
                @Suppress("DEPRECATION")
                Array(MediaCodecList.getCodecCount()) { i -> MediaCodecList.getCodecInfoAt(i) }
            } catch (e: Exception) {
                return mutableListOf()
            }
        }
    }

    if (SDK_INT in 21..23 && mediaCodecInfos.find { it.name.endsWith("secure") } == null) {
        // Some devices don't list secure decoders on API 21 with a newer way of querying codecs,
        // but potentially could also happen on API levels 22 and 23.
        // In that case try the old way.
        try {
            @Suppress("DEPRECATION")
            val oldCodecInfos = Array(MediaCodecList.getCodecCount())
                { i -> MediaCodecList.getCodecInfoAt(i) }.filter { it.name.endsWith("secure") }
            mediaCodecInfos += oldCodecInfos
        } catch (_: Exception) {}
    }

    if (SDK_INT in 22..25 && Build.DEVICE == "R9"
        && mediaCodecInfos.find { it.name == GOOGLE_RAW_DECODER } == null
        && mediaCodecInfos.find { it.name == MEDIATEK_RAW_DECODER } != null) {
        // Oppo R9 does not list a generic raw audio decoder, yet it can be instantiated by name.
        try {
            val rawMediaCodec = MediaCodec.createByCodecName(GOOGLE_RAW_DECODER)
            //noinspection NewApi
            mediaCodecInfos += rawMediaCodec.codecInfo
        } catch (_: Exception) {}
    }

    val showAliases = prefs.getBoolean("show_aliases", false)
    val filteringOption = prefs.getString("filter_type", "2")!!.toInt()
    var codecSimpleInfoList = ArrayList<CodecSimpleInfo>()

    for ((codecIndex, mediaCodecInfo) in mediaCodecInfos.withIndex()) {
        if ((filteringOption == 0 && mediaCodecInfo.isEncoder) || (filteringOption == 1 && !mediaCodecInfo.isEncoder)) {
            continue
        }

        if (SDK_INT >= 29) {
            if (!showAliases && mediaCodecInfo.isAlias) {
                continue
            }
        }

        mediaCodecInfo.supportedTypes.forEachIndexed{ index,  codecId ->
            try {
                mediaCodecInfo.getCapabilitiesForType(codecId)
            } catch (e: Exception) {
                // Some devices (e.g. Kindle Fire HD) can report a codec in the supported list
                // but don't really implement it (or it's buggy). In this case just skip this.
                return@forEachIndexed
            }

            if (codecId.startsWith("wfd")) {
                // This type of video codecs can't be properly queried.
                return@forEachIndexed
            }

            val isAudioCodec = mediaCodecInfo.isAudioCodec()

            if (isAudio == isAudioCodec) {
                val codecSimpleInfo = CodecSimpleInfo((codecIndex * 100 + index).toLong(), codecId, mediaCodecInfo.name,
                        isAudioCodec, mediaCodecInfo.isEncoder, isHardwareAccelerated(mediaCodecInfo))
                if (codecSimpleInfoList.find {
                    it.codecId == codecSimpleInfo.codecId
                            && it.codecName == codecSimpleInfo.codecName
                } == null) {
                    codecSimpleInfoList.add(codecSimpleInfo)
                }
            }
        }
    }

    val sortType = try {
        prefs.getString("sort_type", "0")!!.toInt()
    } catch (e: Exception) {
        prefs.getInt("sort_type", 0)
    }
    val comparator: Comparator<CodecSimpleInfo> = when (sortType) {
        0 -> compareBy({ it.codecId }, { it.codecName })
        1 -> compareByDescending<CodecSimpleInfo> { it.codecId }.thenBy { it.codecName }
        2 -> compareBy({ it.codecName }, { it.codecId })
        else -> compareByDescending<CodecSimpleInfo> { it.codecName }.thenBy { it.codecId }
    }

    codecSimpleInfoList = codecSimpleInfoList.sortedWith(comparator).distinct() as ArrayList<CodecSimpleInfo>

    if (isAudio) {
        audioCodecList.addAll(codecSimpleInfoList)
    } else {
        videoCodecList.addAll(codecSimpleInfoList)
    }

    return codecSimpleInfoList
}

fun getDetailedCodecInfo(context: Context, codecId: String, codecName: String): List<DetailsProperty> {
    val mediaCodecInfo = mediaCodecInfos.find { it.name == codecName } ?: return emptyList()

    // Google uses the same decoder for both DP and non-DP content for MPEG-4,
    // but in the first case codec capabilities can't be queried properly,
    // so copying capabilities for video/mp4v-es seems like the best possible thing to do.
    val overrideM4pvEsdp = codecId == "video/mp4v-esdp" && codecName == "OMX.google.mpeg4.decoder"
    val capabilities = if (!overrideM4pvEsdp) {
        mediaCodecInfo.getCapabilitiesForType(codecId)
    } else {
        mediaCodecInfo.getCapabilitiesForType("video/mp4v-es")
    }

    val isAudio = mediaCodecInfo.isAudioCodec()
    val isEncoder = mediaCodecInfo.isEncoder

    val propertyList = arrayListOf<DetailsProperty>()

    if (SDK_INT >= 29 && mediaCodecInfo.isAlias) {
        propertyList.add(DetailsProperty(propertyList.size.toLong(), context.getString(R.string.alias_for),
                mediaCodecInfo.canonicalName))
    }

    propertyList.add(DetailsProperty(propertyList.size.toLong(), context.getString(R.string.hardware_acceleration),
            isHardwareAccelerated(mediaCodecInfo).toString()))

    propertyList.add(DetailsProperty(propertyList.size.toLong(), context.getString(R.string.software_only),
            isSoftwareOnly(mediaCodecInfo).toString()))

    if (!isEncoder && SDK_INT >= 30) {
        addLowLatencyFeatureIfSupported(context, codecName, capabilities, propertyList)
    }

    propertyList.add(DetailsProperty(propertyList.size.toLong(), context.getString(R.string.codec_provider),
            context.getString(if (isVendor(mediaCodecInfo))
                R.string.codec_provider_oem else R.string.codec_provider_android)))

    if (SDK_INT >= 23) {
        propertyList.add(DetailsProperty(propertyList.size.toLong(), context.getString(R.string.max_instances),
                capabilities.maxSupportedInstances.toString()))
    }

    if (isAudio) {
        if (SDK_INT >= 21) {
            getAudioCapabilities(context, codecId, codecName, capabilities, propertyList)
        }
    } else {
        getVideoCapabilities(context, codecId, codecName, capabilities, propertyList)

        if (!isEncoder) {
            if (SDK_INT >= 19) {
                propertyList.addFeature(context, capabilities, FEATURE_AdaptivePlayback, R.string.adaptive_playback)
            }

            if (SDK_INT >= 26) {
                propertyList.addFeature(context, capabilities, FEATURE_PartialFrame, R.string.partial_frames)
            }

            if (SDK_INT >= 21) {
                propertyList.addFeature(context, capabilities, FEATURE_SecurePlayback, R.string.secure_playback)
            }
        } else {
            if (SDK_INT >= 24) {
                propertyList.addFeature(context, capabilities, FEATURE_IntraRefresh, R.string.intra_refresh)
            }
            if (SDK_INT >= 31) {
                propertyList.addFeature(context, capabilities, FEATURE_QpBounds, R.string.qp_bounds)
            }
            if (SDK_INT >= 33) {
                propertyList.addFeature(context, capabilities, FEATURE_EncodingStatistics, R.string.encoding_statistics)
                propertyList.addFeature(context, capabilities, FEATURE_HdrEditing, R.string.hdr_editing)
            }
        }
    }

    if (SDK_INT >= 29) {
        propertyList.addFeature(context, capabilities, FEATURE_DynamicTimestamp, R.string.dynamic_timestamp)
        propertyList.addFeature(context, capabilities, FEATURE_MultipleFrames, R.string.multiple_access_units)
    }

    if (!isEncoder && SDK_INT >= 21) {
        propertyList.addFeature(context, capabilities, FEATURE_TunneledPlayback, R.string.tunneled_playback)

        if (SDK_INT >= 29) {
            propertyList.addFeature(context, capabilities, FEATURE_FrameParsing, R.string.partial_access_units)
        }
    }

    if (isEncoder && SDK_INT >= 21) {
        val encoderCapabilities = capabilities.encoderCapabilities
        var bitrateModesString =
                "${context.getString(R.string.cbr)}: " +
                        "${encoderCapabilities.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR)}" +
                        "\n${context.getString(R.string.cq)}: " +
                        "${encoderCapabilities.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ)}" +
                        "\n${context.getString(R.string.vbr)}: " +
                        "${encoderCapabilities.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)}"
        if (SDK_INT >= 31) {
            bitrateModesString += "\n${context.getString(R.string.cbr_fd)}: " +
                    "${encoderCapabilities.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR_FD)}"
        }
        propertyList.add(DetailsProperty(propertyList.size.toLong(),
                context.getString(R.string.bitrate_modes), bitrateModesString))

        val defaultMediaFormat = capabilities.defaultFormat
        handleComplexityRange(encoderCapabilities, defaultMediaFormat, context, propertyList)
        handleQualityRange(encoderCapabilities, defaultMediaFormat, propertyList, context)
    }

    if (SDK_INT >= 31) {
        try {
            val codec = MediaCodec.createByCodecName(codecName)
            val vendorParams = codec.supportedVendorParameters
            if (vendorParams.isNotEmpty()) {
                propertyList.add(
                    DetailsProperty(propertyList.size.toLong(),
                    context.getString(R.string.vendor_parameters), vendorParams.joinToString("\n")))
            }
            codec.release()
        } catch (_: Throwable) {}
    }

    val profileString = if (codecId.contains("mp4a-latm") || codecId.contains("wma")) {
        context.getString(R.string.profiles)
    } else {
        context.getString(R.string.profile_levels)
    }

    getProfileLevels(context, codecId, codecName, capabilities)?.let {
        propertyList.add(DetailsProperty(propertyList.size.toLong(), profileString, it))
    }

    return propertyList
}

@RequiresApi(30)
private fun addLowLatencyFeatureIfSupported(context: Context,
                                            codecName: String,
                                            capabilities: MediaCodecInfo.CodecCapabilities,
                                            propertyList: MutableList<DetailsProperty>) {
    if (capabilities.isFeatureSupported(FEATURE_LowLatency)) {
        propertyList.addFeature(context, capabilities, FEATURE_LowLatency, R.string.low_latency)
    } else if (SDK_INT >= 31) {
        var codec: MediaCodec? = null
        try {
            codec = MediaCodec.createByCodecName(codecName)
            val vendorLowLatencyKey = codec.supportedVendorParameters.find { it in knownVendorLowLatencyOptions }
            val featureString = if (vendorLowLatencyKey != null) {
                HtmlCompat.fromHtml(context.getString(R.string.feature_low_latency_vendor_supported, vendorLowLatencyKey),
                    HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            } else {
                false.toString()
            }
            propertyList.add(DetailsProperty(propertyList.size.toLong(), context.getString(R.string.low_latency), featureString))
        } catch (_: Exception) {}
        finally {
            codec?.release()
        }
    }
}

@RequiresApi(21)
private fun handleQualityRange(encoderCapabilities: MediaCodecInfo.EncoderCapabilities,
                               defaultMediaFormat: MediaFormat,
                               propertyList: MutableList<DetailsProperty>,
                               context: Context) {
    @Suppress("UNCHECKED_CAST")
    val qualityRange = if (SDK_INT >= 28) {
        encoderCapabilities.qualityRange
    } else {
        // Before P quality range was actually available, but hidden as a private API.
        try {
            val qualityRangeMethod = encoderCapabilities::class.java
                    .getDeclaredMethod("getQualityRange")
            qualityRangeMethod.invoke(encoderCapabilities) as Range<Int>
        } catch (e: Exception) {
            null
        }
    }

    qualityRange?.let {
        val qualityRangeLower = qualityRange.lower
        val qualityRangeUpper = qualityRange.upper

        var defaultQuality = 0

        if (defaultMediaFormat.containsKey("quality")) {
            defaultQuality = defaultMediaFormat.getInteger("quality")
        }

        if (qualityRangeLower != qualityRangeUpper) {
            propertyList.add(DetailsProperty(propertyList.size.toLong(),
                    context.getString(R.string.quality_range),
                    "$qualityRangeLower — $qualityRangeUpper " +
                        "(${context.getString(R.string.range_default)}: $defaultQuality)"))
        }
    }
}

@RequiresApi(21)
private fun handleComplexityRange(encoderCapabilities: MediaCodecInfo.EncoderCapabilities,
                                  defaultMediaFormat: MediaFormat,
                                  context: Context,
                                  propertyList: MutableList<DetailsProperty>) {
    val complexityLower = encoderCapabilities.complexityRange.lower
    val complexityUpper = encoderCapabilities.complexityRange.upper

    if (complexityLower != complexityUpper) {
        var defaultComplexity = 0

        if (defaultMediaFormat.containsKey(MediaFormat.KEY_COMPLEXITY)) {
            defaultComplexity = defaultMediaFormat.getInteger(MediaFormat.KEY_COMPLEXITY)
        }

        val complexityRangeString = "$complexityLower — $complexityUpper " +
                "(${context.getString(R.string.range_default)}: $defaultComplexity)"

        propertyList.add(DetailsProperty(propertyList.size.toLong(), context.getString(R.string.complexity_range), complexityRangeString))
    }
}

@RequiresApi(21)
private fun getAudioCapabilities(context: Context, codecId: String, codecName: String,
                                 capabilities: MediaCodecInfo.CodecCapabilities,
                                 propertyList: MutableList<DetailsProperty>) {
    val audioCapabilities = capabilities.audioCapabilities

    var minChannelCount = 1
    val maxChannelCount = adjustMaxInputChannelCount(codecId, codecName,
        audioCapabilities.maxInputChannelCount, capabilities)

    if (SDK_INT >= 31) {
        minChannelCount = audioCapabilities.minInputChannelCount
    }

    propertyList.add(DetailsProperty(propertyList.size.toLong(), context.getString(R.string.input_channels),
        if (minChannelCount != maxChannelCount) {
            "$minChannelCount \u2014 $maxChannelCount"
        } else {
            maxChannelCount.toString()
        }
    ))

    val bitrateRangeString = when {
        codecId == "audio/amr-wb-plus" -> // Source: http://www.voiceage.com/AMR-WBplus.html
            "Mono: 6 Kbps \u2014 36 Kbps\nStereo: 7 Kbps \u2014 48 Kbps"

        // Source: https://web.archive.org/web/20070901193343/http://www.microsoft.com/windows/windowsmedia/forpros/codecs/audio.aspx
        codecId.contains("wma") -> when {
            codecId.endsWith("wma-voice")
                    || codecName.contains("wmsVoice", true) -> "4 Kbps \u2014 20 Kbps"
            codecName.endsWith("wma10Pro", true)
                    || codecName.contains("WMAPRODecoder", true)
                    || codecId.endsWith("wma-pro")
                    || codecId.endsWith("wmapro") -> "24 Kbps \u2014 768 Kbps"
            codecName.endsWith("wmaLossLess", true)
                    || codecId.endsWith("wma-lossless") -> "470 Kbps \u2014 940 Kbps"
            else -> "64 Kbps \u2014 192 Kbps"
        }

        else -> {
            val bitrateRange = audioCapabilities.bitrateRange
            if (bitrateRange.lower == bitrateRange.upper || bitrateRange.lower == 1) {
                bitrateRange.upper.toBytesPerSecond()
            } else {
                "${bitrateRange.lower.toBytesPerSecond()} \u2014 ${bitrateRange.upper.toBytesPerSecond()}"
            }
        }
    }

    propertyList.add(DetailsProperty(propertyList.size.toLong(),
            context.getString(R.string.bitrate_range), bitrateRangeString))

    val sampleRates = audioCapabilities.supportedSampleRateRanges
    val sampleRatesString = when {
        // Source: https://github.com/macosforge/alac/blob/master/ReadMe.txt
        codecId.contains("alac") -> "1 kHz \u2014 384 kHz"

        // Source: http://www.3gpp.org/ftp/Specs/html-info/26290.htm
        codecId == "audio/amr-wb-plus" -> {
            "16.0, 24.0, 32.0, 48.0 kHz"
        }

        // Source: https://web.archive.org/web/20070901193343/http://www.microsoft.com/windows/windowsmedia/forpros/codecs/audio.aspx
        codecId.contains("wma") -> when {
            codecId.endsWith("wma-voice")
                    || codecName.contains("wmsVoice", true) -> "8.0, 11.025, 12.0, 16.0, 22.05 kHz"
            codecName.endsWith("wma10Pro", true)
                    || codecName.contains("WMAPRODecoder", true)
                    || codecId.endsWith("wma-pro")
                    || codecId.endsWith("wmapro") -> "8.0, 11.025, 12.0, 16.0, 22.05, 24.0, 32.0, 44.1, 48.0, 88.2, 96.0 kHz"
            codecName.endsWith("wmaLossLess", true)
                    || codecId.endsWith("wma-lossless") -> "8.0, 11.025, 12.0, 16.0, 22.05, 24.0, 32.0, 44.1, 48.0, 88.2, 96.0 kHz"
            else -> "8.0, 11.025, 12.0, 16.0, 22.05, 24.0, 32.0, 44.1, 48.0 kHz"
        }

        sampleRates.size > 1 -> {
            val rates = StringBuilder(sampleRates[0].upper.toKiloHertz().toString())

            for (rate in 1 until sampleRates.size) {
                rates.append(", ").append(sampleRates[rate].upper.toKiloHertz())
            }

            rates.append(" kHz")
            rates.toString()
        }

        else -> {
            var lower = sampleRates[0].lower
            var upper = sampleRates[0].upper

            // Some AC3 codecs, especially on older devices, provide maximum sample rate
            // bigger than it's actually allowed by the spec.
            if (codecId.endsWith("ac3")) {
                lower = min(lower, AC3_MAX_SAMPLE_RATE)
                upper = min(upper, AC3_MAX_SAMPLE_RATE)
            }
            if (lower == upper) {
                "${upper.toKiloHertz()} kHz"
            } else {
                "${lower.toKiloHertz()} kHz \u2014 ${upper.toKiloHertz()} kHz"
            }
        }
    }

    propertyList.add(DetailsProperty(propertyList.size.toLong(),
            context.getString(R.string.sample_rates), sampleRatesString))
}

/**
 * Tries to adjust max input channel count for non-platform codecs.
 *
 * AudioCapabilities incorrectly assumes that non-platform codecs support only one input channel.
 * This function provides a somewhat better, assumed guess.
 */
private fun adjustMaxInputChannelCount(codecId: String, codecName: String, maxChannelCount: Int,
                                       capabilities: MediaCodecInfo.CodecCapabilities): Int {
    if (maxChannelCount != DEFAULT_MAX_INPUT_CHANNEL_LIMIT) {
        if (maxChannelCount > 1) {
            // The maximum channel count looks like it's been set correctly.
            return maxChannelCount
        }

        if (codecId in platformSupportedTypes) {
            // Platform code should have set a default.
            return maxChannelCount
        }
    }

    if (CAN_USE_REFLECTION_FOR_MCAPABILITIESINFO) {
        /*
            mCapabilitiesInfo, a private MediaFormat instance hidden in MediaCodecInfo,
            can actually provide max input channel count (as well as other useful info).
            Android 9.0 put it on a dark greylist, though, so it can't be easily accessed anymore
            (although it is bypassed on a non-store mobile flavor). Newer versions are SOL here.
         */
        try {
            val capabilitiesInfo = capabilities::class.java.getDeclaredField("mCapabilitiesInfo")
            capabilitiesInfo.isAccessible = true
            val mediaFormat = capabilitiesInfo.get(capabilities) as MediaFormat

            if (mediaFormat.containsKey("max-channel-count")) {
                return mediaFormat.getString("max-channel-count")!!.toInt()
            }
        } catch (_: Exception) {}
    }

    if (codecId.endsWith("flac") || codecId.endsWith("alac")) {
        /* Some LG devices have ALAC and FLAC decoders with a non-standard mimetype,
           so the detection below won't work.
           I wouldn't be surprised if other OEMs do the same thing with their codecs.

           Sources for channel count:
           https://github.com/macosforge/alac/blob/c38887c5c5e64a4b31108733bd79ca9b2496d987/codec/ALACAudioTypes.h#L70
           https://xiph.org/flac/faq.html#general__channels
        */
        return 8
    }

    // Some WMA codecs provide their type in the mimetype, some in their name, some in both.
    // Why.
    // Source:
    // https://web.archive.org/web/20070901193343/http://www.microsoft.com/windows/windowsmedia/forpros/codecs/audio.aspx
    if (codecId.contains("wma")) {
        return when {
            codecId.endsWith("wma-voice") || codecName.contains("wmsVoice", true) -> 1
            codecName.endsWith("wma10Pro", true) || codecName.contains("WMAPRODecoder", true)
                    || codecId.endsWith("wma-pro") || codecId.endsWith("wmapro") -> 8
            codecName.endsWith("wmaLossLess", true) || codecId.endsWith("wma-lossless") -> 6
            else -> 2
        }
    }

    // The maximum channel count looks incorrect. Adjust it to an assumed default.
    return when (codecId) {
        "audio/ac3" -> 6
        "audio/ac4" -> 24
        // Source: http://www.voiceage.com/AMR-WBplus.html
        "audio/amr-wb-plus" -> 2
        "audio/dts" -> 8
        "audio/eac3" -> 16
        // source: https://mpeg.chiariglione.org/standards/mpeg-1/audio
        "audio/mpeg", "audio/mpeg-l1", "audio/mpeg-l2" -> 2
        // Default to the platform limit, which is 30.
        else -> DEFAULT_MAX_INPUT_CHANNEL_LIMIT
    }
}

private fun getVideoCapabilities(context: Context, codecId: String, codecName: String,
                                 capabilities: MediaCodecInfo.CodecCapabilities,
                                 propertyList: MutableList<DetailsProperty>) {
    if (SDK_INT >= 21) {
        val videoCapabilities = capabilities.videoCapabilities

        val maxResolution = getMaxResolution(codecId, videoCapabilities)
        propertyList.add(DetailsProperty(propertyList.size.toLong(),
                context.getString(R.string.max_resolution), "${maxResolution[0]}x${maxResolution[1]}"))

        val bitrateRange = videoCapabilities.bitrateRange
        propertyList.add(DetailsProperty(propertyList.size.toLong(),
                context.getString(R.string.max_bitrate),
                bitrateRange.upper.toBytesPerSecond()))

        val framerates = getSupportedFrameRates(codecId, videoCapabilities)
        val framerateString = if (framerates.lower == framerates.upper) {
            "${framerates.upper} ${context.getString(R.string.frames_per_second)}"
        } else {
            "${framerates.lower} \u2014 ${framerates.upper} ${context.getString(R.string.frames_per_second)}"
        }
        propertyList.add(DetailsProperty(propertyList.size.toLong(),
                context.getString(R.string.frame_rate), framerateString))

        val frameRatePerResolutions = getFrameRatePerResolutions(context, codecId, videoCapabilities)
        if (frameRatePerResolutions.isNotEmpty()) {
            propertyList.add(DetailsProperty(propertyList.size.toLong(),
                    context.getString(R.string.max_frame_rate_per_resolution), frameRatePerResolutions))
        }
    }

    addColorFormats(capabilities, codecName, context, propertyList)
}

private fun addColorFormats(capabilities: MediaCodecInfo.CodecCapabilities, codecName: String,
                            context: Context, propertyList: MutableList<DetailsProperty>) {
    val colorFormats = capabilities.colorFormats
    val colorFormatStrings = Array(colorFormats.size) {
        var colorFormat = when {
            codecName.contains("brcm", true) -> BroadcomColorFormat.from(colorFormats[it])
            codecName.contains("qcom", true) || codecName.contains("qti", true)
                    || codecName.contains("ittiam", true)
            -> QualcommColorFormat.from(colorFormats[it])
            codecName.contains("OMX.SEC", true) || codecName.contains("Exynos", true)
            -> SamsungColorFormat.from(colorFormats[it])
            codecName.contains("OMX.MTK", true) -> MediaTekColorFormat.from(colorFormats[it])
            codecName.contains("OMX.IMG", true) -> IMGColorFormat.from(colorFormats[it])
            codecName.contains("Marvell", true) -> MarvellColorFormat.from(colorFormats[it])
            codecName.contains("Nvidia", true) -> NvidiaColorFormat.from(colorFormats[it])
            codecName.contains("OMX.ST", true) -> SonyColorFormat.from(colorFormats[it])
            codecName.contains("Renesas", true) -> RenesasColorFormat.from(colorFormats[it])
            codecName.contains("OMX.PSC", true) || codecName.contains("OMX.SNI", true)
            -> PanasonicSNIColorFormat.from(colorFormats[it])
            codecName.contains("OMX.TI", true) || codecName.contains("INTEL", true)
                    || codecName.contains("OMX.rk", true)
            -> OtherColorFormat.from(colorFormats[it])
            else -> null
        }

        // When in doubt, use a standard color formats from the SDK / OpenMAX IL
        // (at least MSVDX/Topaz codecs tend to use some of those from the second one)
        if (colorFormat == null) {
            colorFormat = StandardColorFormat.from(colorFormats[it])
        }

        getFormattedColorProfileString(context, colorFormat
                ?: context.getString(R.string.unknown), colorFormats[it])
    }.toSortedSet()
    propertyList.add(DetailsProperty(propertyList.size.toLong(),
            context.getString(R.string.color_profiles),
            colorFormatStrings.joinToString("\n")))
}

private fun getFormattedColorProfileString(context: Context, colorFormat: String, colorFormatInt: Int): String {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    return when (prefs.getString("known_values_color_profiles", "1")!!.toInt()) {
        0 -> colorFormat
        1 -> "$colorFormat (${colorFormatInt.toHexHstring()})"
        else -> "$colorFormat ($colorFormatInt)"
    }
}

@RequiresApi(21)
private fun getMaxResolution(codecId: String, videoCapabilities: MediaCodecInfo.VideoCapabilities): IntArray {
    val maxWidth = videoCapabilities.supportedWidths.upper
    val maxHeight = videoCapabilities.supportedHeights.upper
    val defaultResolution = intArrayOf(maxWidth, maxHeight)

    // Some devices (e.g. Samsung, Huawei, and Pixel 6) under-report their encoding
    // capabilities. The supported height reported for H265@3840x2160 is 2144,
    // and H264@1920x1080 is 1072.
    // Cross reference with CamcorderProfile to ensure a resolution is supported.
    if (maxHeight == 1072 && CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P)) {
        defaultResolution[0] = 1920
        defaultResolution[1] = 1080
    } else if (maxHeight == 2144 && CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_2160P)) {
        defaultResolution[0] = 3840
        defaultResolution[1] = 2160
    }

    return if (!areCapabilitiesUnknown(videoCapabilities)) {
        defaultResolution
    } else when {
        codecId.endsWith("divx311") || codecId.endsWith("divx4") -> DIVX4_MAX_RESOLUTION
        codecId.endsWith("divx") -> DIVX6_MAX_RESOLUTION
        else -> {
            defaultResolution
        }
    }
}

@RequiresApi(21)
private fun getSupportedFrameRates(codecId: String, videoCapabilities: MediaCodecInfo.VideoCapabilities): Range<Int> {
    val defaultFrameRates = videoCapabilities.supportedFrameRates

    return if (!areCapabilitiesUnknown(videoCapabilities)) {
        defaultFrameRates
    } else when {
        codecId.endsWith("divx311") || codecId.endsWith("divx4") -> Range(0, DIVX4_480P_MAX_FRAME_RATE)
        codecId.endsWith("divx") -> Range(0, DIVX6_720P_MAX_FRAME_RATE)
        else -> videoCapabilities.supportedFrameRates
    }
}

@RequiresApi(21)
private fun getSupportedFrameRatesFor(codecId: String, videoCapabilities: MediaCodecInfo.VideoCapabilities,
                                      width: Int, height: Int): Range<Double> {
    return if (!areCapabilitiesUnknown(videoCapabilities)) {
        videoCapabilities.getSupportedFrameRatesFor(width, height)
    } else when {
        codecId.endsWith("divx311") || codecId.endsWith("divx4") -> {
            val upper = (if (height < 576) DIVX4_480P_MAX_FRAME_RATE else DIVX4_576P_MAX_FRAME_RATE).toDouble()
            Range(0.0, upper)
        }
        codecId.endsWith("divx") -> {
            val upper = (if (height < 1080) DIVX6_720P_MAX_FRAME_RATE else DIVX6_1080P_MAX_FRAME_RATE).toDouble()
            Range(0.0, upper)
        }
        else -> Range(0.0, getSupportedFrameRates(codecId, videoCapabilities).upper.toDouble())
    }
}

@RequiresApi(21)
private fun getFrameRatePerResolutions(context: Context, codecId: String,
                                       videoCapabilities: MediaCodecInfo.VideoCapabilities): String {
    val capabilities = StringBuilder()
    var maxFrameRate: Double
    val fpsString = context.getString(R.string.frames_per_second)
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val option = prefs.getString("known_resolutions", "0")!!.toInt()
    val maxResolution = getMaxResolution(codecId, videoCapabilities)

    framerateResolutions.forEachIndexed { index, resolution ->
        if (resolution[0] > maxResolution[0]) {
            return@forEachIndexed
        }

        if (videoCapabilities.isSizeSupported(resolution[0], resolution[1])) {
            maxFrameRate = getSupportedFrameRatesFor(codecId, videoCapabilities, resolution[0], resolution[1]).upper

            if (option == 0) {
                capabilities.append("${framerateClasses[index]}: ${"%.1f".format(maxFrameRate)} $fpsString\n")
            } else {
                capabilities.append("${resolution[0]}x${resolution[1]}: ${"%.1f".format(maxFrameRate)} $fpsString\n")
            }
        }
    }

    return capabilities.toString().dropLast(1) // Remove the last \n
}

@SuppressLint("NewApi")
private fun getProfileLevels(context: Context, codecId: String, codecName: String,
                             capabilities: MediaCodecInfo.CodecCapabilities): String? {
    val profileLevels = capabilities.profileLevels
    val stringBuilder = StringBuilder()
    var profile: String?
    var level: String? = ""

    // On Android <=6.0, some devices do not advertise VP9 profile level support.
    // In this case, estimate the level from MediaCodecInfo.VideoCapabilities instead.
    if (SDK_INT <= 23 && codecId.endsWith("vp9") && profileLevels.isEmpty()) {
        val vp9Level = getMaxVP9ProfileLevel(if (SDK_INT >= 21) capabilities else null)
        // Assume all platforms before N only support VP9 profile 0.
        profile = VP9Profiles.VP9Profile0.name
        level = VP9Levels.from(vp9Level)!!
        stringBuilder.append(getFormattedProfileLevelString(context,
                profile, VP9Profiles.VP9Profile0.value, level, vp9Level))

        return stringBuilder.toString()
    } else if (profileLevels.isNullOrEmpty()) {
        return null
    }

    val comparator: Comparator<MediaCodecInfo.CodecProfileLevel> = compareBy { it.profile }
    profileLevels.sortedWith(comparator)
    profileLevels.reversed().distinctBy { it.profile }.reversed().forEach {
        when {
            codecId.contains("mp4a-latm") -> {
                profile = AACProfiles.from(it.profile)
            }
            codecId.contains("ac4") -> {
                profile = AC4Profiles.from(it.profile)
                level = AC4Levels.from(it.level)
            }
            codecId.contains("av01") -> {
                profile = AV1Profiles.from(it.profile)
                level = AV1Levels.from(it.level)
            }
            codecId.contains("avc") -> {
                profile = AVCProfiles.from(it.profile)
                if (profile == null) {
                    if (codecName.contains("qcom", true) || codecName.contains("qti", true)) {
                        profile = AVCQualcommProfiles.from(it.profile)
                    } else if (codecName.contains("OMX.SEC", true) || codecName.contains("Exynos", true)) {
                        profile = AVCSamsungProfiles.from(it.profile)
                    }
                }
                level = AVCLevels.from(it.level)
            }
            codecId.contains("avs") -> {
                profile = AVSProfiles.from(it.profile)
                level = AVSLevels.from(it.level)
            }
            codecId.contains("dolby-vision") -> {
                profile = DolbyVisionProfiles.from(it.profile)
                level = DolbyVisionLevels.from(it.level)
            }
            codecId.contains("vnd.dts.hd") -> {
                profile = DTSHDProfiles.from(it.profile)
            }
            codecId.contains("vnd.dts.uhd") -> {
                profile = DTSUHDProfiles.from(it.profile)
            }
            (codecId.contains("3gpp") && !codecName.contains("mpeg4", true))
                    || codecId.contains("sorenson") || codecId.contains("flv") -> {
                profile = H263Profiles.from(it.profile)
                level = H263Levels.from(it.level)
            }
            codecId.contains("hevc") || codecId.contains("heic") || codecId.contains("heif") -> {
                if (needsHevc10BitProfileExcluded(codecId, it.profile)) {
                    return@forEach
                }
                profile = HEVCProfiles.from(it.profile)
                level = HEVCLevels.from(it.level)
            }
            codecId.endsWith("mpeg") || codecId.contains("mpeg2") -> {
                var extension = " "

                if (codecName.contains("Renesas", true)) {
                    extension = "OMF_MC"
                }

                profile = MPEG2Profiles.from(it.profile, extension)
                level = MPEG2Levels.from(it.level, extension)
            }
            codecId.contains("mp4v-es") || codecId.contains("divx")
                    || codecId.contains("xvid") || codecId.endsWith("mp4")
                    || (codecId.contains("3gpp") && codecName.contains("mpeg4", true)) -> {
                var extension = " "

                if (codecName.contains("qcom", true) || codecName.contains("qti", true)) {
                    extension = "QOMX"
                } else if (codecName.contains("OMX.SEC", true) || codecName.contains("Exynos", true)) {
                    extension = "OMX_SEC"
                } else if (codecName.contains("Renesas", true)) {
                    extension = "OMF_MC"
                }

                profile = MPEG4Profiles.from(it.profile)
                level = MPEG4Levels.from(it.level, extension)
            }
            codecId.contains("mvc") -> {
                profile = MVCProfiles.from(it.profile)
                level = MVCLevels.from(it.level)
            }
            codecId.contains("vc1") || codecId.contains("asf") || codecId.endsWith("wmv9") -> {
                val extension: String? = if (codecName.contains("Renesas", true)) {
                    "OMF_MC"
                } else {
                    null
                }
                profile = VC1Profiles.from(it.profile, extension)
                level = VC1Levels.from(it.level, extension)
            }
            codecId.contains("vp6") -> {
                profile = VP6Profiles.from(it.profile)
                level = null
            }
            codecId.contains("vp8") -> {
                profile = VP8Profiles.from(it.profile)
                level = VP8Levels.from(it.level)
            }
            codecId.contains("vp9") -> {
                profile = VP9Profiles.from(it.profile)
                level = VP9Levels.from(it.level)
            }
            codecId.contains("wma") -> {
                profile = WMAProfiles.from(it.profile)
            }
            codecId.contains("wmv") -> {
                profile = WMVProfiles.from(it.profile)
                level = WMVLevels.from(it.level)
            }
            else -> {
                profile = null
                level = null
            }
        }

        stringBuilder.append(getFormattedProfileLevelString(context,
                profile, it.profile, level, it.level))
    }

    stringBuilder.setLength(stringBuilder.length - 1) // Remove the last \n
    return stringBuilder.toString()
}

private fun getFormattedProfileLevelString(context: Context, profile: String?, profileInt: Int,
                                           level: String?, levelInt: Int): String {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val option = prefs.getString("known_values_profile_levels", "1")!!.toInt()
    val unknownString = context.getString(R.string.unknown)

    val profileString = when (option) {
        0 -> profile ?: unknownString
        1 -> "${profile ?: unknownString} (${profileInt.toHexHstring()})"
        else -> "${profile ?: unknownString} ($profileInt)"
    }

    val levelNameString = level?.ifEmpty { "" } ?: unknownString

    return if (levelNameString.isNotEmpty()) {
        val levelString = when (option) {
            0 -> levelNameString
            1 -> "$levelNameString (${levelInt.toHexHstring()})"
            else -> "$levelNameString ($levelInt)"
        }

        "$profileString: $levelString\n"
    } else {
        "$profileString\n"
    }
}

@RequiresApi(19)
private fun MutableList<DetailsProperty>.addFeature(context: Context,
                                                      capabilities: MediaCodecInfo.CodecCapabilities,
                                                      capability: String,
                                                      @StringRes featureResId: Int) {
    val featureSupported = capabilities.isFeatureSupported(capability)
    val featureValue = if (SDK_INT >= 21 && featureSupported) {
        context.getString(R.string.feature_support_format,
                featureSupported.toString(),
                capabilities.isFeatureRequired(capability).toString())
    } else {
        featureSupported.toString()
    }
    add(DetailsProperty(size.toLong(), context.getString(featureResId), featureValue))
}

/**
 * Needed on M and older to get correct information about VP9 support.
 */
private fun getMaxVP9ProfileLevel(capabilities: MediaCodecInfo.CodecCapabilities?): Int {
    val maxBitrate = if (SDK_INT >= 21 && capabilities?.videoCapabilities != null) {
        capabilities.videoCapabilities.bitrateRange.upper
    } else 0

    // https://www.webmproject.org/vp9/levels
    return when {
        maxBitrate >= 180_000_000 -> VP9Level52.value
        maxBitrate >= 120_000_000 -> VP9Level51.value
        maxBitrate >= 60_000_000  -> VP9Level5.value
        maxBitrate >= 30_000_000  -> VP9Level41.value
        maxBitrate >= 18_000_000  -> VP9Level4.value
        maxBitrate >= 12_000_000  -> VP9Level31.value
        maxBitrate >= 7_200_000   -> VP9Level3.value
        maxBitrate >= 3_600_000   -> VP9Level21.value
        maxBitrate >= 1_800_000   -> VP9Level2.value
        maxBitrate >= 800_000     -> VP9Level11.value
        else                      -> VP9Level1.value    // Assume level 1 is always supported.
    }
}

private fun isVendor(codecInfo: MediaCodecInfo): Boolean {
    val codecName = codecInfo.name.lowercase(Locale.ENGLISH)
    return (!codecName.startsWith("omx.google.")
            && !codecName.startsWith("c2.android.")
            && !codecName.startsWith("c2.google.")
            && !codecName.startsWith("c2.vda.arc")
            && !codecName.startsWith("arc."))
}

private fun isSoftwareOnly(codecInfo: MediaCodecInfo): Boolean {
    if (SDK_INT >= 29) {
        return codecInfo.isSoftwareOnly
    }

    // Hardware audio decoders aren't really a thing, particularly on older devices.
    if (codecInfo.isAudioCodec()) {
        return true
    }

    val codecName = codecInfo.name.lowercase(Locale.ENGLISH)

    // Broadcom codecs which specifically mention HW acceleration in their names
    if (codecName.contains("omx.brcm.video", true) && codecName.contains("hw", true)) {
        return false
    }

    // Marvell codecs which specifically mention HW acceleration in their names
    if (codecName.startsWith("omx.marvell.video.hw", true)) {
        return false
    }

    // Intel codecs which specifically mention HW acceleration in their names
    if (codecName.startsWith("omx.intel.hw_vd", true)) {
        return false
    }

    // Qualcomm codecs which specifically mention HW acceleration in their names
    if (codecName.startsWith("omx.qcom") && codecName.endsWith("hw")) {
        return false
    }

    // ARC/ARC++ (App Runtime for Chrome) codecs are always HW-only.
    if (codecName.startsWith("c2.vda.arc") || codecName.startsWith("arc.")) {
        return false
    }

    return codecName.startsWith("omx.google.")
            || codecName.contains("ffmpeg") // either OMX.ffmpeg or OMX.k3.ffmpeg
            || (codecName.startsWith("omx.sec.") && codecName.contains(".sw."))
            || codecName == "omx.qcom.video.decoder.hevcswvdec"
            || codecName.startsWith("c2.android.")
            || codecName.startsWith("c2.google.")
            || codecName.startsWith("omx.sprd.soft.")
            || codecName.startsWith("omx.avcodec.")
            || codecName.startsWith("omx.pv")
            || codecName.endsWith("sw", true)
            || codecName.endsWith("sw.dec", true)
            || codecName.contains("sw_vd", true)
            || (!codecName.startsWith("omx.") && !codecName.startsWith("c2."))
}

private fun isHardwareAccelerated(codecInfo: MediaCodecInfo): Boolean {
    return if (SDK_INT >= 29) {
        codecInfo.isHardwareAccelerated
    } else {
        !isSoftwareOnly(codecInfo)
    }
}

@RequiresApi(21)
private fun areCapabilitiesUnknown(videoCapabilities: MediaCodecInfo.VideoCapabilities): Boolean {
    return videoCapabilities.supportedFrameRates.upper == DEFAULT_MAX_FRAME_RATE
            && videoCapabilities.supportedWidths.upper == DEFAULT_MAX_SIZE
            && videoCapabilities.supportedHeights.upper == DEFAULT_MAX_SIZE
}

private fun needsHevc10BitProfileExcluded(codecId: String, profile: Int): Boolean {
    // See https://github.com/google/ExoPlayer/issues/3537 for more info.
    return "video/hevc" == codecId && HEVCProfiles.HEVCProfileMain10.value == profile
            && ("sailfish" == Build.DEVICE || "marlin" == Build.DEVICE)
}