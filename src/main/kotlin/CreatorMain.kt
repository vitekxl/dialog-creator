import dialog.DialogCreator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tools.ArgsInput

// -i C:\Users\bilousov\IdeaProjects\dialog-game\scripts -orf C:\Users\bilousov\IdeaProjects\dialog-creator\src\main\resources\routers\routers.json -og C:\Users\bilousov\IdeaProjects\dialog-creator\src\main\resources\output -op C:\Users\bilousov\IdeaProjects\dialog-creator\src\main\resources\output
class CreatorMain {
    companion object{
        private val logger = LoggerFactory.getLogger(CreatorMain::class.java) as Logger
        @JvmStatic
        fun main(args: Array<String>) {
            logger.info("---- DIALOG CREATOR START ----")
            if(ArgsInput.isHelp(args)){
                printHelp()
                logger.info("---- DIALOG CREATOR EXIT ----")
                return
            }
            ArgsInput.readArgs(args)
            ArgsInput.checkConfig()
            logConfigs()
            DialogCreator.createDialogs(Configs.INPUT_SCRIPT_FOLDER)
            logger.info("---- DIALOG CREATOR EXIT ----")
        }

        private fun logConfigs(){
            logger.info("INPUT_SCRIPT_FOLDER  = ${Configs.INPUT_SCRIPT_FOLDER}")
            logger.info("OUTPUT_GRAPHS_FOLDER = ${Configs.OUTPUT_GRAPHS_FOLDER}")
            logger.info("OUTPUT_PHRASES_FOLDER= ${Configs.OUTPUT_PHRASES_FOLDER}")
            logger.info("OUTPUT_ROUTERS_FILE  = ${Configs.OUTPUT_ROUTERS_FILE}")
        }
        private fun printHelp(){
            val message = """
                Input Args:
                 -h  = print this message
                 -i  = INPUT_SCRIPT_FOLDER
                -op  = OUTPUT_PHRASES_FOLDER 
                -og  = OUTPUT_GRAPHS_FOLDER
                -orf = OUTPUT_ROUTERS_FILE 
                -dc  = PHRASE_TEXT_DEF_CLASS (def = ${Configs.PHRASE_TEXT_DEF_CLASS})
            """.trimIndent()
            logger.info(message);
        }
    }
}
