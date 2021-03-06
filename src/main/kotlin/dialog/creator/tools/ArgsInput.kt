package dialog.creator.tools

import dialog.creator.Configs
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException

class ArgsInput {
    companion object{
        private val logger = LoggerFactory.getLogger(ArgsInput::class.java) as Logger

        public fun readArgs(args: Array<String>){
            logger.info(">> readArgs: ${args.contentToString()}")

            val map = hashMapOf<String, String>()

            args.forEachIndexed { i, arg ->
                if(arg.startsWith("--")){
                    map[arg] = "true";
                }
                else if(arg.startsWith("-")) {
                    map[arg] = args[i+1]
                }
            }

            map.keys.forEach{
                when(it){
                    "-i" -> Configs.INPUT_SCRIPT_FOLDER = map[it]!!
                    "-op" -> Configs.OUTPUT_PHRASES_FOLDER = map[it]!!
                    "-og"-> Configs.OUTPUT_GRAPHS_FOLDER = map[it]!!
                    "-orf"-> Configs.OUTPUT_ROUTERS_FILE = map[it]!!
                    "-dc"-> Configs.PHRASE_TEXT_DEF_CLASS = map[it]!!
                    "--create-world-router" -> Configs.CREATE_WORLD_ROUTER = true
                    "-rn" ->  Configs.WORLD_ROUTER_NAME = map[it]!!
                    "-rs" ->  Configs.ROUTER_START_POINT = map[it]!!
                    "-rr" ->  Configs.WORLD_ROUTER_RESET = map[it]!!.toBoolean()
                }
            }
            logger.info("<< readArgs")
        }

        public fun checkConfig(){
            val errorList = arrayListOf<String>();


            try {  Configs.INPUT_SCRIPT_FOLDER  }
            catch (e: Exception){
                errorList.add("INPUT_FOLDER")
            }
            try {   Configs.OUTPUT_GRAPHS_FOLDER  }
            catch (e: Exception){
                errorList.add("OUTPUT_GRAPHS_FOLDER")
            }
            try {  Configs.OUTPUT_PHRASES_FOLDER   }
            catch (e: Exception){
                errorList.add("OUTPUT_PHRASES_FOLDER")
            }
            try { Configs.OUTPUT_ROUTERS_FILE }
            catch (e: Exception){
                errorList.add("OUTPUT_ROUTERS_FILE")
            }

            if (Configs.CREATE_WORLD_ROUTER){
                try { Configs.ROUTER_START_POINT }
                catch (e: Exception){
                    errorList.add("ROUTER_START_POINT")
                }

                try { Configs.WORLD_ROUTER_NAME }
                catch (e: Exception){
                    errorList.add("WORLD_ROUTER_NAME")
                }
                try { Configs.WORLD_ROUTER_RESET}
                catch (e: Exception){
                    errorList.add("WORLD_ROUTER_RESET")
                }
            }


            if(errorList.isNotEmpty()){
                logger.error("some arguments not found: ${errorList.toTypedArray().contentToString()}")
                throw IllegalArgumentException("some arguments not found: ${errorList.toTypedArray().contentToString()}")
            }
        }

        public fun isHelp(args: Array<String>) : Boolean{
            if(args.isEmpty()) return false
             return args[0] == "-h" || args[0] == "-help"
        }
    }
}