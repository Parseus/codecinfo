package com.parseus.codecinfo.codecinfo

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.*
import android.util.Range
import androidx.annotation.RequiresApi
import com.parseus.codecinfo.R
import com.parseus.codecinfo.codecinfo.colorformats.*
import com.parseus.codecinfo.codecinfo.profilelevels.*
import com.parseus.codecinfo.toBytesPerSecond
import com.parseus.codecinfo.toHexHstring
import com.parseus.codecinfo.toKiloHertz

object CodecUtils {

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

    private val mediaCodecInfos: Array<MediaCodecInfo>
    private var audioCodecSimpleInfoList: ArrayList<CodecSimpleInfo> = ArrayList()
    private var videoCodecSimpleInfoList: ArrayList<CodecSimpleInfo> = ArrayList()

    init {
        mediaCodecInfos = if (SDK_INT >= LOLLIPOP) {
            MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos
        } else {
            @Suppress("DEPRECATION")
            Array(MediaCodecList.getCodecCount()) { i -> MediaCodecList.getCodecInfoAt(i)}
        }
    }

    fun getSimpleCodecInfoList(isAudio: Boolean): ArrayList<CodecSimpleInfo> {
        if (isAudio && audioCodecSimpleInfoList.isNotEmpty()) {
            return audioCodecSimpleInfoList
        } else if (!isAudio && videoCodecSimpleInfoList.isNotEmpty()) {
            return videoCodecSimpleInfoList
        }

        val codecSimpleInfoList = ArrayList<CodecSimpleInfo>()

        for (mediaCodecInfo in mediaCodecInfos) {
            for (codecId in mediaCodecInfo.supportedTypes) {
                try {
                    mediaCodecInfo.getCapabilitiesForType(codecId)
                } catch (e: IllegalArgumentException) {
                    // Some devices (e.g. Kindle Fire HD) can report a codec in the supported list
                    // but don't really implement it (or it's buggy). In this case just skip this.
                    continue
                }

                val isAudioCodec = isAudioCodec(mediaCodecInfo)

                if (isAudio && isAudioCodec) {
                    val codecSimpleInfo = CodecSimpleInfo(codecId, mediaCodecInfo.name, isAudioCodec, isEncoder(mediaCodecInfo))
                    codecSimpleInfoList.add(codecSimpleInfo)
                } else if (!isAudio && !isAudioCodec) {
                    val codecSimpleInfo = CodecSimpleInfo(codecId, mediaCodecInfo.name, isAudioCodec, isEncoder(mediaCodecInfo))
                    codecSimpleInfoList.add(codecSimpleInfo)
                }
            }
        }

        val comparator: Comparator<CodecSimpleInfo> = compareBy({it.codecId}, {it.codecName})
        codecSimpleInfoList.sortWith(comparator)

        if (isAudio) {
            audioCodecSimpleInfoList = codecSimpleInfoList
        } else {
            videoCodecSimpleInfoList = codecSimpleInfoList
        }

        return codecSimpleInfoList
    }

    fun getDetailedCodecInfo(context: Context, codecId: String, codecName: String): HashMap<String, String> {
        val mediaCodecInfo = mediaCodecInfos.first { it.name == codecName }
        val capabilities = mediaCodecInfo.getCapabilitiesForType(codecId)
        val isAudio = isAudioCodec(mediaCodecInfo)
        val isEncoder = isEncoder(mediaCodecInfo)

        val codecInfoMap = LinkedHashMap<String, String>()

        if (SDK_INT >= M) {
            codecInfoMap[context.getString(R.string.max_instances)] = capabilities.maxSupportedInstances.toString()
        }

        if (isAudio) {
            if (SDK_INT >= LOLLIPOP) {
                getAudioCapabilities(context, codecId, capabilities, codecInfoMap)
            }
        } else {
            getVideoCapabilities(context, codecName, capabilities, codecInfoMap)

            if (!isEncoder) {
                if (SDK_INT >= KITKAT) {
                    codecInfoMap[context.getString(R.string.adaptive_playback)] =
                            capabilities.isFeatureSupported(
                                    MediaCodecInfo.CodecCapabilities.FEATURE_AdaptivePlayback).toString()
                }

                if (SDK_INT >= O) {
                    codecInfoMap[context.getString(R.string.partial_frames)] =
                            capabilities.isFeatureSupported(
                                    MediaCodecInfo.CodecCapabilities.FEATURE_PartialFrame).toString()
                }

                if (SDK_INT >= LOLLIPOP) {
                    codecInfoMap[context.getString(R.string.secure_playback)] =
                            capabilities.isFeatureSupported(
                                    MediaCodecInfo.CodecCapabilities.FEATURE_SecurePlayback).toString()
                }
            } else {
                if (SDK_INT >= N) {
                    codecInfoMap[context.getString(R.string.intra_refresh)] =
                            capabilities.isFeatureSupported(
                                    MediaCodecInfo.CodecCapabilities.FEATURE_IntraRefresh).toString()
                }
            }
        }

        if (!isEncoder && SDK_INT >= LOLLIPOP) {
            codecInfoMap[context.getString(R.string.tunneled_playback)] =
                    capabilities.isFeatureSupported(
                            MediaCodecInfo.CodecCapabilities.FEATURE_TunneledPlayback).toString()
        }

        if (isEncoder && SDK_INT >= LOLLIPOP) {
            val encoderCapabilities = capabilities.encoderCapabilities
            val bitrateModesString =
                    "${context.getString(R.string.cbr)}: " +
                            "${encoderCapabilities.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR)}" +
                            "\n${context.getString(R.string.cq)}: " +
                            "${encoderCapabilities.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ)}" +
                            "\n${context.getString(R.string.vbr)}: " +
                            "${encoderCapabilities.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)}"
            codecInfoMap[context.getString(R.string.bitrate_modes)] = bitrateModesString

            val complexityLower = encoderCapabilities.complexityRange.lower
            val complexityUpper = encoderCapabilities.complexityRange.upper
            var defaultComplexity = 0

            val defaultMediaFormat = capabilities.defaultFormat
            if (defaultMediaFormat.containsKey(MediaFormat.KEY_COMPLEXITY)) {
                defaultComplexity = defaultMediaFormat.getInteger(MediaFormat.KEY_COMPLEXITY)
            }

            val complexityRangeString = if (complexityLower != complexityUpper) {
                "$complexityLower — $complexityUpper (${context.getString(R.string.range_default)}: $defaultComplexity)"
            } else {
                "$complexityLower"
            }

            codecInfoMap[context.getString(R.string.complexity_range)] = complexityRangeString

            @Suppress("UNCHECKED_CAST")
            val qualityRange = if (SDK_INT >= P) {
                encoderCapabilities.qualityRange
            } else {
                // Before P quality range was actually available, but hidden as a private API.
                try {
                    val qualityRangeMethod = encoderCapabilities::class.java.getDeclaredMethod("getQualityRange")
                    qualityRangeMethod.invoke(encoderCapabilities) as Range<Int>
                } catch (e: Exception) { null }
            }
            
            qualityRange?.let {
                val qualityRangeLower = qualityRange.lower
                val qualityRangeUpper = qualityRange.upper

                val qualityRangeString = if (qualityRangeLower != qualityRangeUpper) {
                    "$qualityRangeLower — $qualityRangeUpper"
                } else {
                    "$qualityRangeLower"
                }

                codecInfoMap[context.getString(R.string.quality_range)] = qualityRangeString
            }
        }

        val profileString = if (codecId.contains("mp4a-latm")) {
            context.getString(R.string.profiles)
        } else {
            context.getString(R.string.profile_levels)
        }

        getProfileLevels(context, codecId, codecName, capabilities)?.let {
            codecInfoMap[profileString] = it
        }

        return codecInfoMap
    }

    @RequiresApi(LOLLIPOP)
    private fun getAudioCapabilities(context: Context, codecId: String, capabilities: MediaCodecInfo.CodecCapabilities,
                                     codecInfoMap: HashMap<String, String>) {
        val audioCapabilities = capabilities.audioCapabilities

        codecInfoMap[context.getString(R.string.input_channels)] =
                adjustMaxInputChannelCount(codecId, audioCapabilities.maxInputChannelCount, capabilities).toString()

        val bitrateRange = audioCapabilities.bitrateRange
        val bitrateRangeString = if (bitrateRange.lower == bitrateRange.upper || bitrateRange.lower == 1) {
            bitrateRange.upper.toBytesPerSecond()
        } else {
            "${bitrateRange.lower.toBytesPerSecond()} \u2014 ${bitrateRange.upper.toBytesPerSecond()}"
        }
        codecInfoMap[context.getString(R.string.bitrate_range)] = bitrateRangeString

        val sampleRates = audioCapabilities.supportedSampleRateRanges
        val sampleRatesString = if (sampleRates.size > 1) {
            val rates = StringBuilder(sampleRates[0].upper.toKiloHertz().toString())

            for (rate in 1 until sampleRates.size) {
                rates.append(", ").append(sampleRates[rate].upper.toKiloHertz())
            }

            rates.append(" KHz")
            rates.toString()
        } else {
            if (sampleRates[0].lower == sampleRates[0].upper) {
                "${sampleRates[0].upper.toKiloHertz()} KHz"
            } else {
                "${sampleRates[0].lower.toKiloHertz()}, ${sampleRates[0].upper.toKiloHertz()} KHz"
            }
        }
        codecInfoMap[context.getString(R.string.sample_rates)] = sampleRatesString
    }

    /**
     * Tries to adjust max input channel count for non-platform codecs.
     *
     * AudioCapabilities incorrectly assumes that non-platform codecs support only one input channel.
     * This function provides a somewhat better, assumed guess.
     */
    @SuppressLint("NewApi")
    private fun adjustMaxInputChannelCount(codecId: String, maxChannelCount: Int, capabilities: MediaCodecInfo.CodecCapabilities): Int {
        if (maxChannelCount > 1 || (SDK_INT >= O && maxChannelCount > 0)) {
            // The maximum channel count looks like it's been set correctly.
            return maxChannelCount
        }

        if (codecId in platformSupportedTypes) {
            // Platform code should have set a default.
            return maxChannelCount
        }

        if (SDK_INT in LOLLIPOP..O_MR1) {
            /*
                mCapabilitiesInfo, a private MediaFormat instance hidden in MediaCodecInfo,
                can actually providemax input channel count (as well as other useful info).
                Unfortunately, with P restricting non-API usage via reflection, I can only hope
                that everything will work fine on newer versions.
             */
            try {
                val capabilitiesInfo = capabilities::class.java.getDeclaredField("mCapabilitiesInfo")
                capabilitiesInfo.isAccessible = true
                val mediaFormat = capabilitiesInfo.get(capabilities) as MediaFormat

                if (mediaFormat.containsKey("max-channel-count")) {
                    return mediaFormat.getString("max-channel-count").toInt()
                }
            } catch (e: Exception) {}
        }

        // The maximum channel count looks incorrect. Adjust it to an assumed default.
        return when (codecId) {
            "audio/ac3" -> 6
            "audio/eac3" -> 16
            // Default to the platform limit, which is 30.
            else -> 30
        }
    }

    private fun getVideoCapabilities(context: Context, codecName: String, capabilities: MediaCodecInfo.CodecCapabilities,
                                     codecInfoMap: HashMap<String, String>) {
        if (SDK_INT >= LOLLIPOP) {
            val videoCapabilities = capabilities.videoCapabilities

            val bitrateRange = videoCapabilities.bitrateRange
            val framerates = videoCapabilities.supportedFrameRates
            val maxWidth = videoCapabilities.supportedWidths.upper
            val maxHeight = videoCapabilities.getSupportedHeightsFor(maxWidth).upper

            val maxResolution = "${maxWidth}x$maxHeight"
            codecInfoMap[context.getString(R.string.max_resolution)] = maxResolution

            codecInfoMap[context.getString(R.string.max_bitrate)] = bitrateRange.upper.toBytesPerSecond()

            val framerateString = if (framerates.lower == framerates.upper) {
                "${framerates.upper} ${context.getString(R.string.frames_per_second)}"
            } else {
                "${framerates.lower} \u2014 ${framerates.upper} ${context.getString(R.string.frames_per_second)}"
            }
            codecInfoMap[context.getString(R.string.frame_rate)] = framerateString

            if (maxHeight >= 360) {
                codecInfoMap[context.getString(R.string.max_frame_rate_per_resolution)] =
                        getFrameRatePerResolutions(context, videoCapabilities)
            }
        }

        val colorFormats = capabilities.colorFormats
        val colorFormatStrings = Array(colorFormats.size) { it ->
            var colorFormat = when {
                codecName.contains("broadcomm", true) -> BroadcomColorFormat.from(colorFormats[it])
                codecName.contains("qcom", true) || codecName.contains("qti", true)
                    -> QualcommColorFormat.from(colorFormats[it])
                codecName.contains("OMX.SEC", true) || codecName.contains("Exynos", true)
                    -> SamsungColorFormat.from(colorFormats[it])
                codecName.contains("OMX.STE", true) || codecName.contains("OMX.TI", true)
                    || codecName.contains("INTEL", true) -> OtherColorFormat.from(colorFormats[it])
                codecName.contains("OMX.MTK", true) -> MediaTekColorFormat.from(colorFormats[it])
                else -> null
            }

            if (colorFormat == null) {
                colorFormat = MediaCodecColorFormat.from(colorFormats[it])
            }

            colorFormat ?: "${context.getString(R.string.unknown)} (${colorFormats[it].toHexHstring()})"}
        colorFormatStrings.sort()
        codecInfoMap[context.getString(R.string.color_profiles)] = colorFormatStrings.joinToString("\n")
    }

    @RequiresApi(LOLLIPOP)
    private fun getFrameRatePerResolutions(context: Context, videoCapabilities: MediaCodecInfo.VideoCapabilities): String {
        val capabilities = StringBuilder()
        var maxFrameRate: Double
        val fpsString = context.getString(R.string.frames_per_second)

        if (videoCapabilities.isSizeSupported(480, 360)) {
            maxFrameRate = videoCapabilities.getSupportedFrameRatesFor(480, 360).upper
            capabilities.append("360p: ").append("%.1f".format(maxFrameRate)).append(" $fpsString")
        }

        if (videoCapabilities.isSizeSupported(640, 480)) {
            maxFrameRate = videoCapabilities.getSupportedFrameRatesFor(640, 480).upper
            capabilities.append("\n480p: ").append("%.1f".format(maxFrameRate)).append(" $fpsString")
        }

        if (videoCapabilities.isSizeSupported(1280, 720)) {
            maxFrameRate = videoCapabilities.getSupportedFrameRatesFor(1280, 720).upper
            capabilities.append("\n720p: ").append("%.1f".format(maxFrameRate)).append(" $fpsString")
        }

        if (videoCapabilities.isSizeSupported(1920, 1080)) {
            maxFrameRate = videoCapabilities.getSupportedFrameRatesFor(1920, 1080).upper
            capabilities.append("\n1080p: ").append("%.1f".format(maxFrameRate)).append(" $fpsString")
        }

        if (videoCapabilities.isSizeSupported(3840, 2160)) {
            maxFrameRate = videoCapabilities.getSupportedFrameRatesFor(3840, 2160).upper
            capabilities.append("\n4K: ").append("%.1f".format(maxFrameRate)).append(" $fpsString")
        }

        return capabilities.toString()
    }

    private fun getProfileLevels(context: Context, codecId: String, codecName: String, capabilities: MediaCodecInfo.CodecCapabilities): String? {
        val profileLevels = capabilities.profileLevels

        if (profileLevels == null || profileLevels.isEmpty()) {
            return null
        }

        val stringBuilder = StringBuilder()
        val unknownString = context.getString(R.string.unknown)
        var profile: String
        var level = ""

        val comparator: Comparator<MediaCodecInfo.CodecProfileLevel> = compareBy{it.profile}
        profileLevels.sortedWith(comparator)
        profileLevels.forEach {
            when {
                codecId.contains("mp4a-latm", true) -> {
                    profile = AACProfiles.from(it.profile) ?: "$unknownString (${it.profile.toHexHstring()})"
                }
                codecId.contains("avc", true) -> {
                    var extension = ""

                    if (codecName.contains("qcom", true) || codecName.contains("qti", true)) {
                        extension = "QOMX"
                    }

                    profile = AVCProfiles.from(it.profile, extension) ?: "$unknownString (${it.profile.toHexHstring()})"
                    level = AVCLevels.from(it.level) ?: "$unknownString (${it.level.toHexHstring()})"
                }
                codecId.contains("divx", true) -> {
                    var extension = ""

                    if (codecName.contains("qcom", true) || codecName.contains("qti", true)) {
                        extension = "QOMX"
                    }

                    profile = DivXProfiles.from(it.profile, extension) ?: "$unknownString (${it.profile.toHexHstring()})"
                    level = "$unknownString (${it.level.toHexHstring()})"
                }
                codecId.contains("dolby-vision", true) -> {
                    profile = DolbyVisionProfiles.from(it.profile) ?: "$unknownString (${it.profile.toHexHstring()})"
                    level = DolbyVisionLevels.from(it.level) ?: "$unknownString (${it.level.toHexHstring()})"
                }
                codecId.contains("3gpp", true) || codecId.contains("sorenson", true) -> {
                    profile = H263Profiles.from(it.profile) ?: "$unknownString (${it.profile.toHexHstring()})"
                    level = H263Levels.from(it.level) ?: "$unknownString (${it.level.toHexHstring()})"
                }
                codecId.contains("hevc", true) -> {
                    profile = HEVCProfiles.from(it.profile) ?: "$unknownString (${it.profile.toHexHstring()})"
                    level = HEVCLevels.from(it.level) ?: "$unknownString (${it.level.toHexHstring()})"
                }
                codecId.contains("mpeg2", true) -> {
                    profile = MPEG2Profiles.from(it.profile) ?: "$unknownString (${it.profile.toHexHstring()})"
                    level = MPEG2Levels.from(it.level) ?: "$unknownString (${it.level.toHexHstring()})"
                }
                codecId.contains("mp4v-es", true) -> {
                    var extension = ""

                    if (codecName.contains("qcom", true) || codecName.contains("qti", true)) {
                        extension = "QOMX"
                    } else if (codecName.contains("OMX.SEC", true) || codecName.contains("Exynos", true)) {
                        extension = "OMX_SEC"
                    }

                    profile = MPEG4Profiles.from(it.profile) ?: "$unknownString (${it.profile.toHexHstring()})"
                    level = MPEG4Levels.from(it.level, extension) ?: "$unknownString (${it.level.toHexHstring()})"
                }
                codecId.contains("vc1", true) || codecId.contains("wmv") -> {
                    var extension = ""

                    if (codecName.contains("qcom", true) || codecName.contains("qti", true)) {
                        extension = "QOMX"
                    }

                    profile = VC1Profiles.from(it.profile, extension) ?: "$unknownString (${it.profile.toHexHstring()})"
                    level = VC1Levels.from(it.level, extension) ?: "$unknownString (${it.level.toHexHstring()})"
                }
                codecId.contains("vp8", true) -> {
                    profile = VP8Profiles.from(it.profile) ?: "$unknownString (${it.profile.toHexHstring()})"
                    level = VP8Levels.from(it.level) ?: "$unknownString (${it.level.toHexHstring()})"
                }
                codecId.contains("vp9", true) -> {
                    profile = VP9Profiles.from(it.profile) ?: "$unknownString (${it.profile.toHexHstring()})"
                    level = VP9Levels.from(it.level) ?: "$unknownString (${it.level.toHexHstring()})"
                }
                else -> {
                    profile = "$unknownString (${it.profile.toHexHstring()})"
                    level = "$unknownString (${it.level.toHexHstring()})"
                }
            }

            stringBuilder.append(if (level.isNotEmpty()) "$profile: $level\n" else "$profile\n")
        }

        stringBuilder.setLength(stringBuilder.length - 1) // Remove the last \n
        return stringBuilder.toString()
    }

    private fun isAudioCodec(mediaCodecInfo: MediaCodecInfo) = mediaCodecInfo.supportedTypes.joinToString().contains("audio")
    private fun isEncoder(mediaCodecInfo: MediaCodecInfo) = mediaCodecInfo.isEncoder

}