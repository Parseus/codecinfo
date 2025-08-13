package com.parseus.codecinfo.ui.settings

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.databinding.WallpaperColorsAdapterItemBinding
import com.parseus.codecinfo.utils.isColorLight

class ColorPickerAdapter(context: Context,
                         private val selectedColor: Int?,
                         private val colors: List<Int>,
                         private val onColorPicked: (Int) -> Unit)
    : RecyclerView.Adapter<ColorPickerAdapter.ViewHolder>() {

    private val layoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getItemCount() = colors.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(WallpaperColorsAdapterItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colors[position]
        with(holder.binding) {
            val tooltipText = "#%02X".format(color and 0xFFFFFF)
            holder.binding.root.apply {
                contentDescription = tooltipText
                TooltipCompat.setTooltipText(this, tooltipText)
            }
            itemColorPickerBackground.background.setTint(color)
            itemColorPickerCheck.isVisible = color == selectedColor
            val tintColor = if (isColorLight(color)) Color.BLACK else Color.WHITE
            itemColorPickerCheck.setImageTintList(ColorStateList.valueOf(tintColor))
            itemColorPickerBackground.setOnClickListener {
                onColorPicked.invoke(color)
            }
        }
    }

    data class ViewHolder(val binding: WallpaperColorsAdapterItemBinding) : RecyclerView.ViewHolder(binding.root)
}