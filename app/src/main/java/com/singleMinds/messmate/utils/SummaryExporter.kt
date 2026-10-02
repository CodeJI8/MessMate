package com.singleminds.messmate.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.singleminds.messmate.ui.MainUiState
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

object SummaryExporter {

    fun shareSummaryImage(context: Context, state: MainUiState) {
        val width = 800
        val height = 1200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw Warm Kitchen Background
        canvas.drawColor(Color.parseColor("#14110F")) // Charcoal Base

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#F4EBDD") // Warm Cream
            textSize = 40f
        }

        val saffronPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#F2A93B") // Saffron
            textSize = 56f
            isFakeBoldText = true
        }

        val subPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#FAF4EA")
            textSize = 32f
        }

        val flatName = state.flat?.name ?: "Messmate Flat"
        val monthLabel = state.selectedMonth?.label ?: "Current Month"
        val currency = state.flat?.currencySymbol ?: "$"

        canvas.drawText("MESSMATE SUMMARY", 60f, 100f, saffronPaint)
        canvas.drawText(flatName, 60f, 170f, paint)
        canvas.drawText(monthLabel, 60f, 220f, subPaint)

        val totalSpentStr = String.format("%.2f", state.totalSpent / 100.0)
        val mealRateStr = String.format("%.2f", state.mealRate)

        canvas.drawText("Total Spent: $currency$totalSpentStr", 60f, 320f, paint)
        canvas.drawText("Meal Rate: $currency$mealRateStr / meal", 60f, 380f, saffronPaint)

        var y = 480f
        canvas.drawText("Member Balances:", 60f, y, subPaint)
        y += 50f

        state.members.forEach { member ->
            val summary = state.summaryMap[member.id]
            val net = summary?.netBalance ?: 0L
            val netStr = String.format("%.2f", abs(net / 100.0))
            val sign = if (net >= 0) "Owed +" else "Owes -"
            canvas.drawText("${member.name}: $sign$currency$netStr", 80f, y, paint)
            y += 45f
        }

        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "messmate_summary.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Summary Card"))
    }
}
