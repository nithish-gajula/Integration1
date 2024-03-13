import android.util.Log
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object LOGGING {

    fun INFO(context: String, msg: String) {
        Log.i(context, msg)
        val log =
            "\n${getCurrentDateTime()}   $context   INFO \n $msg \n --------------------------------------------"
        val markdownContent = "\n*${getCurrentDateTime()}*\t**$context**\t[INFO](#) \n" +
                "> $msg " +
                "\n\n\n"
        reportLog(log, markdownContent)
    }

    fun DEBUG(context: String, msg: String) {
        Log.d(context, msg)
        val log =
            "\n${getCurrentDateTime()}   $context   INFO \n $msg \n --------------------------------------------"
        val markdownContent = "\n*${getCurrentDateTime()}*\t**$context**\t[DEBUG](#) \n" +
                "> $msg " +
                "\n\n\n"
        reportLog(log, markdownContent)
    }

    private fun getCurrentDateTime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss.SSS")
        return currentDateTime.format(formatter)
    }

    private fun reportLog(log: String, markdown: String) {
        FileWriter(ActivityUtils.reportedLogsFile, true).use { it.write(log) }
        FileWriter(ActivityUtils.reportedReadmeLogsFile, true).use { it.write(markdown) }

    }
}