package com.example.musicplayer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.audiofx.Visualizer
import android.util.AttributeSet
import android.view.View

class CustomVisualizerView : View {
    private var colorIndex = 0 // Index to track the current color
    private var bytes: ByteArray? = null
    private var visualizer: Visualizer? = null
    private val paint: Paint = Paint()
    private val colors = intArrayOf(Color.RED, Color.GREEN, Color.BLUE) // Define your colors here

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        paint.strokeWidth = 10f // Set the width of the bars
        paint.strokeCap = Paint.Cap.ROUND // Make the ends of the bars round
    }

    fun releaseVisualizer() {
        visualizer?.release()
        visualizer = null
    }

    fun setPlayer(audioSessionId: Int) {
        visualizer = Visualizer(audioSessionId)
        visualizer?.apply {
            captureSize = Visualizer.getCaptureSizeRange()[1]
            setDataCaptureListener(
                object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        visualizer: Visualizer,
                        waveform: ByteArray,
                        samplingRate: Int
                    ) {
                        bytes = waveform
                        postInvalidate()
                    }

                    override fun onFftDataCapture(
                        visualizer: Visualizer,
                        fft: ByteArray,
                        samplingRate: Int
                    ) {
                        // Not needed for waveform visualizer
                    }
                },
                Visualizer.getMaxCaptureRate() / 2,
                true,
                false
            )
            enabled = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bytes != null) {
            val width = width
            val height = height
            val scale = 111f
            val barWidth = width / (bytes!!.size.toFloat() * 0.06) // Increase the gap between bars
            val thicknessScale = 10 // Adjust the scaling factor to increase thickness

            // Define an array of colors
            val colors = arrayOf(
                Color.RED, Color.GREEN, Color.BLUE,
                Color.YELLOW, Color.CYAN, Color.MAGENTA
            )
            for (i in bytes!!.indices) {
                val barX = i * barWidth * 1.5 // Adjust the position with increased gap
                val amplitude = (bytes!![i] + 128).toFloat() / scale // Normalize the amplitude
                val barHeight = amplitude * height / 2 * thicknessScale // Increase thickness

                // Draw only bars with significant amplitude (adjust threshold as needed)
                if (barHeight < height * 0.9) { // Example threshold: 90% of the view height
                    // Assign color based on the colorIndex
                    paint.color = colors[colorIndex]

                    // Draw the bar
                    canvas.drawRect(
                        barX.toFloat(), (height - barHeight).toFloat(),
                        (barX + barWidth).toFloat(), height.toFloat(), paint
                    )
                }
            }

            // Decrease amplitude of all bars slowly
            for (i in bytes!!.indices) {
                if (bytes!![i] < 128) {
                    bytes!![i] = (bytes!![i] + 1).toByte()
                }
            }
            postInvalidateDelayed(0) // Redraw after a short delay

            // Update the color index for the next draw
            colorIndex = (colorIndex + 1) % colors.size
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        visualizer?.release()
    }
}
