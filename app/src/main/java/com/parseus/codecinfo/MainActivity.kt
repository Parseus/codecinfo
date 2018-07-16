package com.parseus.codecinfo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import com.google.android.material.tabs.TabLayout
import com.parseus.codecinfo.adapters.PagerAdapter
import com.parseus.codecinfo.codecinfo.CodecUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val tabs = tabLayout
        val viewPager = pager.apply {
            val pagerAdapter = PagerAdapter(this@MainActivity, supportFragmentManager)
            adapter = pagerAdapter
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        }
        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab?.position ?: 0
            }
        })
        tabs.setupWithViewPager(viewPager)
    }

    @SuppressLint("InflateParams")
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId

        if (id == R.id.menu_item_share) {
            val codecStringBuilder = StringBuilder()
            val codecSimpleInfoList = CodecUtils.getSimpleCodecInfoList(true)
            codecSimpleInfoList.addAll(CodecUtils.getSimpleCodecInfoList(false))

            for (info in codecSimpleInfoList) {
                codecStringBuilder.append("$info\n")
            }

            ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain").setText(codecStringBuilder.toString()).startChooser()

            return true
        } else if (id == R.id.menu_item_about_app) {
            val alertDialogBuilder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.about_app_dialog, null)
            alertDialogBuilder.setView(dialogView)
            val alertDialog = alertDialogBuilder.create()

            val okButton: View = dialogView.findViewById(R.id.ok_button)
            okButton.setOnClickListener { alertDialog.dismiss() }

            try {
                val versionTextView: TextView = dialogView.findViewById(R.id.version_text_view)
                versionTextView.text = getString(R.string.app_version, packageManager.getPackageInfo(packageName, 0).versionName)
            } catch (e : Exception) {}

            alertDialog.show()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

}
