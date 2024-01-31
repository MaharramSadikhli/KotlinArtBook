package com.imsoft.kotlinartbook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.imsoft.kotlinartbook.databinding.ActivityDetailsBinding

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun saveBtnClick(view: View) {}
    fun selectImageClick(view: View) {}
}