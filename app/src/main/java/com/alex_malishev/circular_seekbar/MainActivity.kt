package com.alex_malishev.circular_seekbar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object{
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        roundSeekBar.setSeekListener(object : CircularSeekBar.SeekListener{
            override fun onProgressChanged(seekBar: CircularSeekBar, progress: Long, byUser: Boolean) {
                Log.e(TAG, "Progress was changed to $progress. Did user change it? - $byUser")
            }

            override fun onStartTrackingTouch(seekBar: CircularSeekBar) {
                seekBar.setPrimaryColor(R.color.colorPrimary)
                seekBar.setTextColor(R.color.colorPrimary)
                Log.e(TAG, "User is starting to change current value")
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar) {
                Log.e(TAG, "User stopped to change current value")
                seekBar.setPrimaryColor(R.color.colorAccent)
                seekBar.setTextColor(android.R.color.darker_gray)
            }
        })
    }
}
