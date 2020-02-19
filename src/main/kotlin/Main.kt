import dialog.DialogCreator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tools.ArgsInput

class Main {
    companion object{
        private val logger = LoggerFactory.getLogger(Main::class.java) as Logger
        @JvmStatic
        fun main(args: Array<String>) {
            logger.info("---- DIALOG CREATOR START ----")
            ArgsInput.readArgs(args)
            ArgsInput.checkConfig()
            logConfigs()
            DialogCreator.createDialogs(Configs.INPUT_SCRIPT_FOLDER)
            logger.info("---- DIALOG CREATOR EXIT ----")
        }

        private fun logConfigs(){
            logger.info("INPUT_SCRIPT_FOLDER  = ${Configs.INPUT_SCRIPT_FOLDER  }")
            logger.info("OUTPUT_GRAPHS_FOLDER = ${Configs.OUTPUT_GRAPHS_FOLDER }")
            logger.info("OUTPUT_PHRASES_FOLDER= ${Configs.OUTPUT_PHRASES_FOLDER}")
            logger.info("OUTPUT_ROUTERS_FILE  = ${Configs.OUTPUT_ROUTERS_FILE  }")
        }
    }
}
