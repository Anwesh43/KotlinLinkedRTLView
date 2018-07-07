package com.anwesh.uiprojects.kotlinlinkedrotthreelineview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.anwesh.uiprojects.linkedrotthreelineview.LinkedRotThreeLineView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view : LinkedRotThreeLineView = LinkedRotThreeLineView.create(this)
        view.addOnCompletionListener({ Toast.makeText(this,"${it} completed",Toast.LENGTH_SHORT).show()},
                { Toast.makeText(this,"${it} is reset",Toast.LENGTH_SHORT).show()})
        fullScreen()
    }
}

fun MainActivity.fullScreen() {
    supportActionBar?.hide()
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}
