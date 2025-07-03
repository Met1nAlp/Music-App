package com.example.retrofitmusic

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class CarouselPageTransformer : ViewPager2.PageTransformer
{

    override fun transformPage(page: View, position: Float)
    {
        page.apply {
            val pageWidth = width.toFloat()
            val pageHeight = height.toFloat()

            when {
                position < -1 -> {
                    alpha = 0f
                }
                position <= 1 -> {
                    alpha = 1f

                    val minScale = 0.75f
                    val scaleFactor = minScale + (1 - minScale) * (1 - abs(position))

                    scaleX = scaleFactor
                    scaleY = scaleFactor


                    val spacing = pageWidth * 0.1f
                    translationX = -position * spacing


                    val verticalOffset = abs(position) * pageHeight * 0.05f
                    translationY = verticalOffset


                    elevation = (1 - abs(position)) * 10f
                }
                else -> {
                    alpha = 0f
                }
            }
        }
    }
}