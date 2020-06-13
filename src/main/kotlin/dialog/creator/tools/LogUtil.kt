package dialog.creator.tools

import dialog.creator.dialog.DialogCreator
import org.slf4j.Logger

class LogUtil(private val logger: Logger) {
    companion object{

        public val errList = StaticList.list

        public fun logStep(logger: Logger, stepName: String) {
            logger.info("")
            logger.info("-------- $stepName --------")
            logger.info("")
        }

        public fun logError(logger: Logger, msg: String) {
            logError(logger, StaticList.list, msg)
        }

        public fun logError(logger: Logger, errorListMessages: MutableList<String>,  msg: String) {
            logger.warn(msg)
            errorListMessages.add(msg)
        }
    }
    public fun logStep( stepName: String) {
        LogUtil.logStep(logger, stepName);
    }

    public fun logError(msg: String) {
        LogUtil.logError(logger, StaticList.list, msg)
    }

    public fun logError( errorListMessages: MutableList<String>,  msg: String) {
        LogUtil.logError(logger, StaticList.list, msg)
    }


}