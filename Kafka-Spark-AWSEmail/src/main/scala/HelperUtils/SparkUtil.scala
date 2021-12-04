package HelperUtils

/**
* SparkUtil
* Utility function for Spark Aggregation and Processing
*/
class SparkUtil
object SparkUtil {
    /**
    *  Extracts LogLevel from the log string.
    *  If it matches the user specified logLevel, it returns 1 else 0
    * 
    *  @param log       log string
    *  @param logLevel  String. (WARN|ERROR|INFO|DEBUG)
    */
    def checkLogLevel(log: String, logLevel: String): Int = {
        val logSplit = log.split(" ")
        // Check if log string has the logLevel
        if (logSplit.length < 2) return 0;
        val logType = logSplit(2)
        if (logType == logLevel) {
            return 1
        }
        return 0
    }

    /**
    *  Check if the log string is ERROR log
    *  @param log       log string
    */
    def isError = (log: String) => checkLogLevel(log, Constants.ERROR_LOG_LEVEL)

    /**
    *  Check if the log string is WARN log
    *  @param log       log string
    */
    def isWarning = (log: String) => checkLogLevel(log, Constants.WARN_LOG_LEVEL)

    /**
    *  Check if the log string is DEBUG log
    *  @param log       log string
    */
    def isDebug = (log: String) => checkLogLevel(log, Constants.DEBUG_LOG_LEVEL)

    /**
    *  Check if the log string is INFO log
    *  @param log       log string
    */
    def isInfo = (log: String) => checkLogLevel(log, Constants.INFO_LOG_LEVEL)

    /**
    *  Combine Partitions helper method.
    *  returns sum of two partitions.
    */
    def combinePartitions = (p1: Int, p2: Int) => p1 + p2

}