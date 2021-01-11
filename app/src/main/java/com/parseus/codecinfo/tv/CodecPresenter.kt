package com.parseus.codecinfo.tv

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.parseus.codecinfo.R
import com.parseus.codecinfo.codecinfo.CodecSimpleInfo

class CodecPresenter(@DrawableRes private val drawable: Int) : Presenter() {

    class ViewHolder(view: View) : Presenter.ViewHolder(view) {
        val cardView = view as ImageCardView
        lateinit var simpleInfo: CodecSimpleInfo
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setBackgroundColor(ContextCompat.getColor(parent.context, R.color.teal_700))
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
        val info = item as CodecSimpleInfo
        (viewHolder as ViewHolder).simpleInfo = info
        viewHolder.cardView.apply {
            titleText = info.codecId
            contentText = info.codecName

            cardType = ImageCardView.CARD_TYPE_INFO_UNDER
            infoVisibility = ImageCardView.CARD_REGION_VISIBLE_ALWAYS

            setMainImageDimensions(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT)
            mainImage = ContextCompat.getDrawable(context, drawable)
            setMainImageScaleType(ImageView.ScaleType.CENTER_INSIDE)
        }
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {}

    companion object {
        private const val GRID_ITEM_WIDTH = 300
        private const val GRID_ITEM_HEIGHT = 200
    }

}