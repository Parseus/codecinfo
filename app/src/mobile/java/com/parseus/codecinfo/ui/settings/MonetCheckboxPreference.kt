package com.parseus.codecinfo.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.CompoundButton
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceViewHolder
import com.parseus.codecinfo.utils.updateColors

class MonetCheckboxPreference : CheckBoxPreference {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val checkbox = holder.findViewById(android.R.id.checkbox) as? CompoundButton
        checkbox?.updateColors(context)
    }

}