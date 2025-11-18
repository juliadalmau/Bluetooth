package com.example.bluetooth.model.export

import android.net.Uri
import android.os.Environment
import com.example.bluetooth.model.data.MeasurementSession
import java.io.File
import java.io.FileWriter
import kotlin.math.max

object CsvExporter {
    fun exportToCSV(session: MeasurementSession): Uri {
        val fileName = "measurement_${session.sessionId}_${session.startTime}.csv"

        // Use Android's Downloads directory
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        val file = File(downloadsDir, fileName)

        FileWriter(file).use { writer ->
            // Write header
            writer.append("Timestamp,Algorithm1_Angle,Algorithm2_Angle\n")

            // Combine both algorithm results
            val maxSize = max(
                session.algorithm1Results.size,
                session.algorithm2Results.size
            )

            for (i in 0 until maxSize) {
                val reading1 = session.algorithm1Results.getOrNull(i)
                val reading2 = session.algorithm2Results.getOrNull(i)

                val timestamp = reading1?.timestamp ?: reading2?.timestamp ?: 0
                val angle1 = reading1?.elevationAngle?.toString() ?: ""
                val angle2 = reading2?.elevationAngle?.toString() ?: ""

                writer.append("$timestamp,$angle1,$angle2\n")
            }
        }

        return Uri.fromFile(file)
    }
}