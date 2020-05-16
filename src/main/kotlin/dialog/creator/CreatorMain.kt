package dialog.creator

import dialog.creator.dialog.DialogCreator
import dialog.creator.router.RouterProperties
import dialog.creator.router.WorldRouterCreator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import dialog.creator.tools.ArgsInput

/**
 * -i
"/Users/viktor.bilousov/Library/Mobile Documents/com~apple~CloudDocs/JavaProjects/dialog-game/scripts"
-orf
"/Users/viktor.bilousov/Library/Mobile Documents/com~apple~CloudDocs/JavaProjects/dialog-creator/src/main/resources/routers/routers.json"
-og
"/Users/viktor.bilousov/Library/Mobile Documents/com~apple~CloudDocs/JavaProjects/dialog-creator/src/main/resources/graphs"
-op
"/Users/viktor.bilousov/Library/Mobile Documents/com~apple~CloudDocs/JavaProjects/dialog-creator/src/main/resources/phrases"
 */
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
           val res = DialogCreator(
                Configs.OUTPUT_PHRASES_FOLDER,
                Configs.OUTPUT_ROUTERS_FILE,
                Configs.OUTPUT_GRAPHS_FOLDER
            ).createAndWriteDialogs(Configs.INPUT_SCRIPT_FOLDER)

            if(!res){
                logger.error("DIALOG created with errors")
                return
            }

            if(Configs.CREATE_WORLD_ROUTER){

                logger.info("----- CREATE WORLD ROUTER ---- ")

                WorldRouterCreator.createAndWrite(
                    RouterProperties(Configs.WORLD_ROUTER_NAME, Configs.WORLD_ROUTER_RESET, Configs.ROUTER_START_POINT),
                    Configs.OUTPUT_ROUTERS_FILE,
                    Configs.OUTPUT_GRAPHS_FOLDER,
                    Configs.OUTPUT_PHRASES_FOLDER
                )
            }
            logger.info("---- DIALOG CREATOR EXIT ----")
        }

        private fun logConfigs() {
            logger.info("VERSION = ${Configs.VERSION}")
            logger.info("INPUT_SCRIPT_FOLDER  = ${Configs.INPUT_SCRIPT_FOLDER}")
            logger.info("OUTPUT_GRAPHS_FOLDER = ${Configs.OUTPUT_GRAPHS_FOLDER}")
            logger.info("OUTPUT_PHRASES_FOLDER= ${Configs.OUTPUT_PHRASES_FOLDER}")
            logger.info("OUTPUT_ROUTERS_FILE  = ${Configs.OUTPUT_ROUTERS_FILE}")
            logger.info("PHRASE_TEXT_DEF_CLASS = ${Configs.PHRASE_TEXT_DEF_CLASS}")
            logger.info("CREATE_WORLD_ROUTER = ${Configs.CREATE_WORLD_ROUTER}")
            if (Configs.CREATE_WORLD_ROUTER) {
                logger.info("ROUTER_START_POINT = ${Configs.ROUTER_START_POINT}")
                logger.info("WORLD_ROUTER_RESET = ${Configs.WORLD_ROUTER_RESET}")
                logger.info("WORLD_ROUTER_NAME = ${Configs.WORLD_ROUTER_NAME}")
            }
        }
        private fun printHelp(){
            val message = """
                Dialogs creator v{${Configs.VERSION}
                Input Args:
                ------------ dialogs ---------------
                 
                -i  = INPUT_SCRIPT_FOLDER
                -op  = OUTPUT_PHRASES_FOLDER 
                -og  = OUTPUT_GRAPHS_FOLDER
                -orf = OUTPUT_ROUTERS_FILE 
                -dc  = PHRASE_TEXT_DEF_CLASS (def = ${Configs.PHRASE_TEXT_DEF_CLASS})
                
                ------------ world router ------------
                
                --create-world-router
                
                -rn = router name (def = ${Configs.WORLD_ROUTER_NAME})
                -rs = router start point 
                -rr = is reset to start (true/false) (def = ${Configs.WORLD_ROUTER_RESET})
                
                ----------- other --------------
                --help  = print this message
                
            """.trimIndent()
            logger.info(message);
        }
    }
}
