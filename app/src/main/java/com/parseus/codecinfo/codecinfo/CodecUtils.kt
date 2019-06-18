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
import androidx.preference.PreferenceManager
import com.parseus.codecinfo.*
import com.parseus.codecinfo.codecinfo.colorformats.*
import com.parseus.codecinfo.codecinfo.profilelevels.*
import com.parseus.codecinfo.codecinfo.profilelevels.VP9Levels.*

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

    private var mediaCodecInfos: Array<MediaCodecInfo> = emptyArray()
    private var audioCodecSimpleInfoList: ArrayList<CodecSimpleInfo> = ArrayList()
    private var videoCodecSimpleInfoList: ArrayList<CodecSimpleInfo> = ArrayList()


    fun getSimpleCodecInfoList(context: Context, isAudio: Boolean): ArrayList<CodecSimpleInfo> {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        if (mediaCodecInfos.isEmpty()) {
            mediaCodecInfos = if (SDK_INT >= LOLLIPOP) {
                MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos
            } else {
                @Suppress("DEPRECATION")
                Array(MediaCodecList.getCodecCount()) { i -> MediaCodecList.getCodecInfoAt(i)}
            }
        }

        var codecSimpleInfoList = ArrayList<CodecSimpleInfo>()

        for (mediaCodecInfo in mediaCodecInfos) {
            for (codecId in mediaCodecInfo.supportedTypes) {
                try {
                    mediaCodecInfo.getCapabilitiesForType(codecId)
                } catch (e: Exception) {
                    // Some devices (e.g. Kindle Fire HD) can report a codec in the supported list
                    // but don't really implement it (or it's buggy). In this case just skip this.
                    continue
                }

                val isAudioCodec = mediaCodecInfo.isAudioCodec()

                if (isAudio == isAudioCodec) {
                    val option = prefs.getString("filter_type", "2")!!.toInt()

                    if ((option == 0 && mediaCodecInfo.isEncoder) || (option == 1 && !mediaCodecInfo.isEncoder)) {
                        continue
                    }

                    val codecSimpleInfo = CodecSimpleInfo(codecId, mediaCodecInfo.name, isAudioCodec,
                            mediaCodecInfo.isEncoder)
                    codecSimpleInfoList.add(codecSimpleInfo)
                }
            }
        }

        val comparator: Comparator<CodecSimpleInfo> = when (prefs.getString("sort_type", "0")!!.toInt()) {
            0 -> compareBy({it.codecId}, {it.codecName})
            1 -> compareByDescending<CodecSimpleInfo>{it.codecId}.thenBy{it.codecName}
            2 -> compareBy({it.codecName}, {it.codecId})
            else -> compareByDescending<CodecSimpleInfo>{it.codecName}.thenBy{it.codecId}
        }

        codecSimpleInfoList = codecSimpleInfoList.sortedWith(comparator).distinct() as ArrayList<CodecSimpleInfo>

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
        val isAudio = mediaCodecInfo.isAudioCodec()
        val isEncoder = mediaCodecInfo.isEncoder

        val codecInfoMap = LinkedHashMap<String, String>()

        codecInfoMap[context.getString(R.string.hardware_acceleration)] =
                mediaCodecInfo.isAccelerated().toString()

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

        if (SDK_INT >= Q) {
            codecInfoMap[context.getString(R.string.dynamic_timestamp)] =
                    capabilities.isFeatureSupported(
                            MediaCodecInfo.CodecCapabilities.FEATURE_DynamicTimestamp).toString()

            codecInfoMap[context.getString(R.string.multiple_access_units)] =
                    capabilities.isFeatureSupported(
                            MediaCodecInfo.CodecCapabilities.FEATURE_MultipleFrames).toString()
        }

        if (!isEncoder && SDK_INT >= LOLLIPOP) {
            codecInfoMap[context.getString(R.string.tunneled_playback)] =
                    capabilities.isFeatureSupported(
                            MediaCodecInfo.CodecCapabilities.FEATURE_TunneledPlayback).toString()

            if (SDK_INT >= Q) {
                codecInfoMap[context.getString(R.string.partial_access_units)] =
                        capabilities.isFeatureSupported(
                                MediaCodecInfo.CodecCapabilities.FEATURE_FrameParsing).toString()
            }
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

            val defaultMediaFormat = capabilities.defaultFormat

            val complexityLower = encoderCapabilities.complexityRange.lower
            val complexityUpper = encoderCapabilities.complexityRange.upper

            if (complexityLower != complexityUpper) {
                var defaultComplexity = 0

                if (defaultMediaFormat.containsKey(MediaFormat.KEY_COMPLEXITY)) {
                    defaultComplexity = defaultMediaFormat.getInteger(MediaFormat.KEY_COMPLEXITY)
                }

                val complexityRangeString = "$complexityLower — $complexityUpper " +
                        "(${context.getString(R.string.range_default)}: $defaultComplexity)"

                codecInfoMap[context.getString(R.string.complexity_range)] = complexityRangeString
            }

            @Suppress("UNCHECKED_CAST")
            val qualityRange = if (SDK_INT >= P) {
                encoderCapabilities.qualityRange
            } else {
                // Before P quality range was actually available, but hidden as a private API.
                try {
                    val qualityRangeMethod = encoderCapabilities::class.java
                            .getDeclaredMethod("getQualityRange")
                    qualityRangeMethod.invoke(encoderCapabilities) as Range<Int>
                } catch (e: Exception) { null }
            }
            
            qualityRange?.let {
                val qualityRangeLower = qualityRange.lower
                val qualityRangeUpper = qualityRange.upper

                var defaultQuality = 0

                if (defaultMediaFormat.containsKey("quality")) {
                    defaultQuality = defaultMediaFormat.getInteger("quality")
                }

                if (qualityRangeLower != qualityRangeUpper) {
                    codecInfoMap[context.getString(R.string.quality_range)] =
                            "$qualityRangeLower — $qualityRangeUpper " +
                                    "(${context.getString(R.string.range_default)}: $defaultQuality)"
                }
            }
        }

        val profileString = if (codecId.contains("mp4a-latm") || codecId.contains("wma")) {
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
    private fun getAudioCapabilities(context: Context, codecId: String,
                                     capabilities: MediaCodecInfo.CodecCapabilities,
                                     codecInfoMap: HashMap<String, String>) {
        val audioCapabilities = capabilities.audioCapabilities

        codecInfoMap[context.getString(R.string.input_channels)] =
                adjustMaxInputChannelCount(codecId, audioCapabilities.maxInputChannelCount, capabilities).toString()

        val bitrateRangeString = if (codecId != "audio/amr-wb-plus") {
            val bitrateRange = audioCapabilities.bitrateRange
            if (bitrateRange.lower == bitrateRange.upper || bitrateRange.lower == 1) {
                bitrateRange.upper.toBytesPerSecond()
            } else {
                "${bitrateRange.lower.toBytesPerSecond()} \u2014 ${bitrateRange.upper.toBytesPerSecond()}"
            }
        } else {
            // Source: http://www.voiceage.com/AMR-WBplus.html
            "Mono: 6 \u2014 36 Kbps\nStereo: 7 \u2014 48 Kbps"
        }

        codecInfoMap[context.getString(R.string.bitrate_range)] = bitrateRangeString

        val sampleRates = audioCapabilities.supportedSampleRateRanges
        val sampleRatesString = when {
            // Source: http://www.3gpp.org/ftp/Specs/html-info/26290.htm
            codecId == "audio/amr-wb-plus" -> {
                "16.0, 24.0, 32.0, 48.0 KHz"
            }

            sampleRates.size > 1 -> {
                val rates = StringBuilder(sampleRates[0].upper.toKiloHertz().toString())

                for (rate in 1 until sampleRates.size) {
                    rates.append(", ").append(sampleRates[rate].upper.toKiloHertz())
                }

                rates.append(" KHz")
                rates.toString()
            }

            else -> {
                if (sampleRates[0].lower == sampleRates[0].upper) {
                    "${sampleRates[0].upper.toKiloHertz()} KHz"
                } else {
                    "${sampleRates[0].lower.toKiloHertz()}, ${sampleRates[0].upper.toKiloHertz()} KHz"
                }
            }
        }

        codecInfoMap [context.getString(R.string.sample_rates)] = sampleRatesString
    }

    /**
     * Tries to adjust max input channel count for non-platform codecs.
     *
     * AudioCapabilities incorrectly assumes that non-platform codecs support only one input channel.
     * This function provides a somewhat better, assumed guess.
     */
    private fun adjustMaxInputChannelCount(codecId: String, maxChannelCount: Int,
                                           capabilities: MediaCodecInfo.CodecCapabilities): Int {
        val platformLimit = 30

        if (maxChannelCount != platformLimit) {
            if (maxChannelCount > 1) {
                // The maximum channel count looks like it's been set correctly.
                return maxChannelCount
            }

            if (codecId in platformSupportedTypes) {
                // Platform code should have set a default.
                return maxChannelCount
            }
        }

        if (SDK_INT < P) {
            /*
                mCapabilitiesInfo, a private MediaFormat instance hidden in MediaCodecInfo,
                can actually provide max input channel count (as well as other useful info).
                Unfortunately, with P restricting non-API usage via reflection, I can only hope
                that everything will work fine on newer versions.
             */
            try {
                val capabilitiesInfo = capabilities::class.java.getDeclaredField("mCapabilitiesInfo")
                capabilitiesInfo.isAccessible = true
                val mediaFormat = capabilitiesInfo.get(capabilities) as MediaFormat

                if (mediaFormat.containsKey("max-channel-count")) {
                    return mediaFormat.getString("max-channel-count")!!.toInt()
                }
            } catch (e: Exception) {}
        }

        if (codecId.endsWith("flac")) {
            /* LG G Pad 8.3 has a FLAC decoder with an audio/x-lg-flac mimetype,
               so normal detection won't work.
               I wouldn't be surprised if other OEMs do the same thing with their codecs.

               Source for channel count: https://xiph.org/flac/faq.html#general__channels
            */
            return 8
        }

        // The maximum channel count looks incorrect. Adjust it to an assumed default.
        return when (codecId) {
            "audio/ac3" -> 6
            // Source: http://www.voiceage.com/AMR-WBplus.html
            "audio/amr-wb-plus" -> 2
            "audio/dts" -> 8
            "audio/eac3" -> 16
            // source: https://mpeg.chiariglione.org/standards/mpeg-1/audio
            "audio/mpeg", "audio/mpeg-l1", "audio/mpeg-l2" -> 2
            // Default to the platform limit, which is 30.
            else -> platformLimit
        }
    }

    private fun getVideoCapabilities(context: Context, codecName: String,
                                     capabilities: MediaCodecInfo.CodecCapabilities,
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

            codecInfoMap[context.getString(R.string.max_frame_rate_per_resolution)] =
                    getFrameRatePerResolutions(context, videoCapabilities)
        }

        addColorFormats(capabilities, codecName, context, codecInfoMap)
    }

    private fun addColorFormats(capabilities: MediaCodecInfo.CodecCapabilities, codecName: String,
                                context: Context, codecInfoMap: HashMap<String, String>) {
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
                codecName.contains("OMX.TI", true) || codecName.contains("INTEL", true)
                -> OtherColorFormat.from(colorFormats[it])
                else -> null
            }

            // When in doubt, use a standard color formats from the SDK...
            if (colorFormat == null) {
                colorFormat = MediaCodecColorFormat.from(colorFormats[it])
            }

            // ...unless it's a standard OpenMAX IL color format that's not defined in the SDK
            // (at least MSVDX/Topaz codecs tend to use some of those)
            if (colorFormat == null) {
                colorFormat = OpenMAXILColorFormat.from(colorFormats[it])
            }

            getFormattedColorProfileString(context, colorFormat ?: context.getString(R.string.unknown), colorFormats[it])
        }.toSortedSet()
        codecInfoMap[context.getString(R.string.color_profiles)] = colorFormatStrings.joinToString("\n")
    }

    private fun getFormattedColorProfileString(context: Context, colorFormat: String, colorFormatInt: Int): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        return when (prefs.getString("known_values_color_profiles", "0")!!.toInt()) {
            0 -> colorFormat
            1 -> "$colorFormat (${colorFormatInt.toHexHstring()})"
            else -> "$colorFormat ($colorFormatInt)"
        }
    }

    @RequiresApi(LOLLIPOP)
    private fun getFrameRatePerResolutions(context: Context,
                                           videoCapabilities: MediaCodecInfo.VideoCapabilities): String {
        val capabilities = StringBuilder()
        var maxFrameRate: Double
        val fpsString = context.getString(R.string.frames_per_second)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val option = prefs.getString("known_resolutions", "0")!!.toInt()

        framerateResolutions.forEachIndexed { index, resolution ->
            if (videoCapabilities.isSizeSupported(resolution[0], resolution[1])) {
                maxFrameRate = videoCapabilities.getSupportedFrameRatesFor(resolution[0], resolution[1]).upper

                if (option == 0) {
                    capabilities.append("${framerateClasses[index]}: ${"%.1f".format(maxFrameRate)} $fpsString\n")
                } else {
                    capabilities.append("${resolution[0]}x${resolution[1]}: ${"%.1f".format(maxFrameRate)} $fpsString\n")
                }
            }
        }

        capabilities.setLength(capabilities.length - 1) // Remove the last \n

        return capabilities.toString()
    }

    @SuppressLint("NewApi")
    private fun getProfileLevels(context: Context, codecId: String, codecName: String,
                                 capabilities: MediaCodecInfo.CodecCapabilities): String? {
        val profileLevels = capabilities.profileLevels
        val stringBuilder = StringBuilder()
        var profile: String?
        var level: String? = ""

        // On versions L and M, VP9 codecCapabilities do not advertise profile level support.
        // In this case, estimate the level from MediaCodecInfo.VideoCapabilities instead.
        // Assume VP9 is not supported before L.
        if (SDK_INT in LOLLIPOP..M && codecId.endsWith("vp9")) {
            val vp9Level = getMaxVP9ProfileLevel(capabilities.videoCapabilities)
            // Assume all platforms before N only support VP9 profile 0.
            profile = VP9Profiles.VP9Profile0.name
            level = VP9Levels.from(vp9Level)!!
            stringBuilder.append(getFormattedProfileLevelString(context,
                    profile, VP9Profiles.VP9Profile0.value, level, vp9Level))

            return stringBuilder.toString()
        } else if (profileLevels == null || profileLevels.isEmpty()) {
            return null
        }

        val comparator: Comparator<MediaCodecInfo.CodecProfileLevel> = compareBy{it.profile}
        profileLevels.sortedWith(comparator)
        profileLevels.reversed().distinctBy { it.profile }.reversed().forEach {
            when {
                codecId.contains("mp4a-latm") -> {
                    profile = AACProfiles.from(it.profile)
                }
                codecId.contains("av01") -> {
                    profile = AV1Profiles.from(it.profile)
                    level = AV1Levels.from(it.level)
                }
                codecId.contains("avc") -> {
                    var extension = " "

                    if (codecName.contains("qcom", true) || codecName.contains("qti", true)) {
                        extension = "QOMX"
                    }

                    profile = AVCProfiles.from(it.profile, extension)
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
                (codecId.contains("3gpp") && !codecName.contains("mpeg4", true))
                        || codecId.contains("sorenson") || codecId.contains("flv") -> {
                    profile = H263Profiles.from(it.profile)
                    level = H263Levels.from(it.level)
                }
                codecId.contains("hevc") -> {
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
        val option = prefs.getString("known_values_profile_levels", "0")!!.toInt()
        val unknownString = context.getString(R.string.unknown)

        val profileString = when (option) {
            0 -> profile ?: unknownString
            1 -> "${profile ?: unknownString} (${profileInt.toHexHstring()})"
            else -> "${profile ?: unknownString} ($profileInt)"
        }

        val levelNameString = if (level != null) {
            if (level.isNotEmpty()) level else ""
        } else {
            unknownString
        }

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

    /**
     * Needed on M and older to get correct information about VP9 support.
     */
    @RequiresApi(LOLLIPOP)
    private fun getMaxVP9ProfileLevel(capabilities: MediaCodecInfo.VideoCapabilities): Int {
        // https://www.webmproject.org/vp9/levels
        val bitrateMapping = arrayOf(
                intArrayOf(200, VP9Level1.value), intArrayOf(800, VP9Level11.value),
                intArrayOf(1800, VP9Level2.value), intArrayOf(3600, VP9Level21.value),
                intArrayOf(7200, VP9Level3.value), intArrayOf(12000, VP9Level31.value),
                intArrayOf(18000, VP9Level4.value), intArrayOf(30000, VP9Level41.value),
                intArrayOf(60000, VP9Level5.value), intArrayOf(120000, VP9Level51.value),
                intArrayOf(180000, VP9Level52.value))
        var maxBitrateRange = 0

        for (entry in bitrateMapping) {
            val bitrate = entry[0]
            val level = entry[1]
            if (capabilities.bitrateRange.contains(bitrate)) {
                maxBitrateRange = level
            }
        }

        return maxBitrateRange
    }