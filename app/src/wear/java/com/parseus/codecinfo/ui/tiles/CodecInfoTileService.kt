package com.parseus.codecinfo.ui.tiles

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.*
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.Material3TileService
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.data.drm.getSimpleDrmInfoList
import com.parseus.codecinfo.ui.WearMainActivity

class CodecInfoTileService : Material3TileService() {

    override suspend fun MaterialScope.tileResponse(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val codecCount = (getSimpleCodecInfoList(context, true).size
                + getSimpleCodecInfoList(context, false).size)
        val drmCount = getSimpleDrmInfoList(context).size

        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(
                                LayoutElementBuilders.Layout.Builder()
                                    .setRoot(layout(context, codecCount, drmCount))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun MaterialScope.layout(
        context: Context,
        codecCount: Int,
        drmCount: Int
    ): LayoutElementBuilders.LayoutElement {
        return primaryLayout(
            mainSlot = {
                textDataCard(
                    onClick = launchAppAction(),
                    title = { LayoutElementBuilders.Row.Builder().build() },
                    content = {
                        LayoutElementBuilders.Column.Builder()
                            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                            .addContent(
                                text(
                                    context.getString(R.string.tile_codec_count, codecCount).layoutString,
                                    typography = Typography.TITLE_MEDIUM,
                                    color = colorScheme.onPrimary
                                )
                            )
                            .addContent(
                                text(
                                    context.getString(R.string.tile_drm_count, drmCount).layoutString,
                                    typography = Typography.TITLE_MEDIUM,
                                    color = colorScheme.onPrimary
                                )
                            )
                            .build()
                    }

                )
            },
            titleSlot = {
                text(context.getString(R.string.app_name).layoutString)
            },
            bottomSlot = {
                textEdgeButton(
                    onClick = launchAppAction(),
                    labelContent = {
                        text(context.getString(R.string.tile_launch_app).layoutString)
                    }
                )
            }
        )
    }

    private fun launchAppAction(): ModifiersBuilders.Clickable {
        return ModifiersBuilders.Clickable.Builder()
            .setOnClick(
                ActionBuilders.LaunchAction.Builder()
                    .setAndroidActivity(
                        ActionBuilders.AndroidActivity.Builder()
                            .setPackageName(packageName)
                            .setClassName(WearMainActivity::class.java.name)
                            .build()
                    )
                    .build()
            )
            .setId("launch_app")
            .build()
    }

    companion object {
        private const val RESOURCES_VERSION = "1"
    }
}