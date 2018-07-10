package com.parseus.codecinfo.codecinfo

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build
import androidx.annotation.RequiresApi
import com.parseus.codecinfo.R
import com.parseus.codecinfo.codecinfo.profilelevels.*
import com.parseus.codecinfo.toBytesPerSecond
import com.parseus.codecinfo.toHexHstring
import com.parseus.codecinfo.toKiloHertz

object CodecUtils {

    private val mediaCodecInfos: Array<MediaCodecInfo>
    private var audioCodecSimpleInfoList: ArrayList<CodecSimpleInfo> = ArrayList(0)
    private var videoCodecSimpleInfoList: ArrayList<CodecSimpleInfo> = ArrayList(0)

    init {
        mediaCodecInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                    val codecSimpleInfo = CodecSimpleInfo(codecId, mediaCodecInfo.name, isAudioCodec)
                    codecSimpleInfoList.add(codecSimpleInfo)
                } else if (!isAudio && !isAudioCodec) {
                    val codecSimpleInfo = CodecSimpleInfo(codecId, mediaCodecInfo.name, isAudioCodec)
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

    fun getDetailedCodecInfo(context: Context, codecId: String, codecName: String): Map<String, String> {
        val mediaCodecInfo = mediaCodecInfos.first { it.name == codecName }
        val capabilities = mediaCodecInfo.getCapabilitiesForType(codecId)
        val isAudio = isAudioCodec(mediaCodecInfo)
        val isEncoder = isEncoder(mediaCodecInfo)

        val codecInfoMap = LinkedHashMap<String, String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            codecInfoMap[context.getString(R.string.max_instances)] = capabilities.maxSupportedInstances.toString()
        }

        if (isAudio) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getAudioCapabilities(context, capabilities, codecInfoMap)
            }
        } else {
            getVideoCapabilities(context, capabilities, codecInfoMap)

            if (!isEncoder) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    codecInfoMap[context.getString(R.string.adaptive_playback)] =
                            capabilities.isFeatureSupported(
                                    MediaCodecInfo.CodecCapabilities.FEATURE_AdaptivePlayback).toString()
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    codecInfoMap[context.getString(R.string.partial_frames)] =
                            capabilities.isFeatureSupported(
                                    MediaCodecInfo.CodecCapabilities.FEATURE_PartialFrame).toString()
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    codecInfoMap[context.getString(R.string.secure_playback)] =
                            capabilities.isFeatureSupported(
                                    MediaCodecInfo.CodecCapabilities.FEATURE_SecurePlayback).toString()
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    codecInfoMap[context.getString(R.string.intra_refresh)] =
                            capabilities.isFeatureSupported(
                                    MediaCodecInfo.CodecCapabilities.FEATURE_IntraRefresh).toString()
                }
            }
        }

        if (!isEncoder && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            codecInfoMap[context.getString(R.string.tunneled_playback)] =
                    capabilities.isFeatureSupported(
                            MediaCodecInfo.CodecCapabilities.FEATURE_TunneledPlayback).toString()
        }

        if (isEncoder && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val encoderCapabilities = capabilities.encoderCapabilities
            val bitrateModesString =
                    "${context.getString(R.string.cbr)}: " +
                            "${encoderCapabilities.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR)}" +
                            "\n${context.getString(R.string.cq)}: " +
                            "${encoderCapabilities.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ)}" +
                            "\n${context.getString(R.string.vbr)}: " +
                            "${encoderCapabilities.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)}"
            codecInfoMap[context.getString(R.string.bitrate_modes)] = bitrateModesString
        }

        getProfileLevels(context, codecId, capabilities)?.let {
            codecInfoMap[context.getString(R.string.profile_levels)] = it
        }

        return codecInfoMap
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getAudioCapabilities(context: Context, capabilities: MediaCodecInfo.CodecCapabilities,
                                     codecInfoMap: HashMap<String, String>) {
        val audioCapabilities = capabilities.audioCapabilities

        codecInfoMap[context.getString(R.string.input_channels)] = audioCapabilities.maxInputChannelCount.toString()

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

    private fun getVideoCapabilities(context: Context, capabilities: MediaCodecInfo.CodecCapabilities,
                                     codecInfoMap: HashMap<String, String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
            ColorFormat.from(colorFormats[it])
                    ?: "${context.getString(R.string.unknown)} (0x${colorFormats[it].toString(16).toUpperCase()})"}
        colorFormatStrings.sort()
        codecInfoMap[context.getString(R.string.color_profiles)] = colorFormatStrings.joinToString("\n")
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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

    private fun getProfileLevels(context: Context, codecId: String, capabilities: MediaCodecInfo.CodecCapabilities): String? {
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
                    profile = AVCProfiles.from(it.profile) ?: "$unknownString (${it.profile.toHexHstring()})"
                    level = AVCLevels.from(it.level) ?: "$unknownString (${it.level.toHexHstring()})"
                }
                codecId.contains("divx", true) -> {
                    profile = DivXProfiles.from(it.profile) ?: "$unknownString (${it.profile.toHexHstring()})"
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
                    profile = MPEG4Profiles.from(it.profile) ?: "$unknownString (${it.profile.toHexHstring()})"
                    level = MPEG4Levels.from(it.level) ?: "$unknownString (${it.level.toHexHstring()})"
                }
                codecId.contains("vc1", true) || codecId.contains("wmv9") -> {
                    profile = VC1Profiles.from(it.profile) ?: "$unknownString (${it.profile.toHexHstring()})"
                    level = VC1Levels.from(it.level) ?: "$unknownString (${it.level.toHexHstring()})"
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