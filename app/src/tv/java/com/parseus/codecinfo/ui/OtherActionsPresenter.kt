package com.parseus.codecinfo.ui

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.parseus.codecinfo.R

data class OtherActionDescriptor(
        val actionId: Int,
        @DrawableRes val drawableId: Int,
        @StringRes val nameResId: Int
)

class OtherActionsPresenter : Presenter() {

    class ViewHolder(view: View) : Presenter.ViewHolder(view) {
        val cardView = view as ImageCardView
        lateinit var descriptor: OtherActionDescriptor
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
        val descriptor = item as OtherActionDescriptor
        (viewHolder as ViewHolder).descriptor = descriptor
        viewHolder.cardView.apply {
            titleText = context.getString(descriptor.nameResId)

            cardType = ImageCardView.CARD_TYPE_INFO_UNDER
            infoVisibility = ImageCardView.CARD_REGION_VISIBLE_ALWAYS

            setMainImageDimensions(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT)
            mainImage = AppCompatResources.getDrawable(context, descriptor.drawableId)
            setMainImageScaleType(ImageView.ScaleType.CENTER_INSIDE)
        }
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {}

    companion object {
        private const val GRID_ITEM_WIDTH = 300
        private const val GRID_ITEM_HEIGHT = 200
    }

}