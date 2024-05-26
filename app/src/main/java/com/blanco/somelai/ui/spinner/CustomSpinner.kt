package com.blanco.somelai.ui.custom

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.view.Window
import android.widget.ImageView
import com.blanco.somelai.R

class CustomSpinner(context: Context) : Dialog(context) {

    private var spinnerImage: ImageView
    private var frameAnimation: AnimationDrawable

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.custom_spinner)
        spinnerImage = findViewById(R.id.spinner_image)
        spinnerImage.setBackgroundResource(R.drawable.robot_spinner_animation)
        frameAnimation = spinnerImage.background as AnimationDrawable
    }

    override fun onStart() {
        super.onStart()
        frameAnimation.start()
    }

    override fun onStop() {
        super.onStop()
        frameAnimation.stop()
    }
}
