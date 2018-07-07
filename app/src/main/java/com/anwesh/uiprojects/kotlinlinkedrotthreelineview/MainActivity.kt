package com.anwesh.uiprojects.kotlinlinkedrotthreelineview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.linkedrotthreelineview.LinkedRotThreeLineView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LinkedRotThreeLineView.create(this)
    }
}
