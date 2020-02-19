package tools

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException

class ArgsInput {
    companion object{
        private val logger = LoggerFactory.getLogger(ArgsInput::class.java) as Logger

        public fun readArgs(args: Array<String>){
            logger.info(">> readArgs: ${args.contentToString()}")


            if(args.size%2 != 0) throw IllegalArgumentException("error argument format");
            val map = hashMapOf<String, String>()
            for(i in args.indices step 2){
                map[args[i]] = args[i+1];
            }
            map.keys.forEach{
                when(it){
                    "-i" -> Configs.INPUT_SCRIPT_FOLDER = map[it]!!
                    "-op" -> Configs.OUTPUT_PHRASES_FOLDER = map[it]!!
                    "-og"-> Configs.OUTPUT_GRAPHS_FOLDER = map[it]!!
                    "-orf"-> Configs.OUTPUT_ROUTERS_FILE = map[it]!!
                    "-dc"-> Configs.PHRASE_TEXT_DEF_CLASS= map[it]!!
                }
            }
            logger.info("<< readArgs")
        }

        public fun checkConfig(){
            val errorList = arrayListOf<String>();
            try {  Configs.INPUT_SCRIPT_FOLDER  }catch (e: Exception){
                errorList.add("INPUT_FOLDER")
            }
            try {   Configs.OUTPUT_GRAPHS_FOLDER  }catch (e: Exception){
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

            if(errorList.isNotEmpty()){
                logger.error("some arguments not found: ${errorList.toTypedArray().contentToString()}")
                throw IllegalArgumentException("some arguments not found: ${errorList.toTypedArray().contentToString()}")
            }
        }
    }
}