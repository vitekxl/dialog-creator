package tools

import java.lang.IllegalArgumentException

class ArgsInput {
    companion object{
        public fun readArgs(args: Array<String>){
            if(args.size%2 != 0) throw IllegalArgumentException("error argument format");
            val map = hashMapOf<String, String>()
            for(i in args.indices step 2){
                map[args[i]] = args[i+1];
            }
            map.keys.forEach{
                when(it){
                    "-i" -> Configs.INPUT_FOLDER = map[it]!!
                    "-op" -> Configs.OUTPUT_PHRASES_FOLDER = map[it]!!
                    "-og"-> Configs.OUTPUT_GRAPHS_FOLDER = map[it]!!
                    "-dc"-> Configs.PHRASE_TEXT_DEF_CLASS= map[it]!!
                }
            }
        }

        public fun checkConfig(){
            val errorList = arrayListOf<String>();
            try {
                Configs.INPUT_FOLDER;

            }catch (e: Exception){
                errorList.add("INPUT_FOLDER")
            }
            try {
                Configs.OUTPUT_GRAPHS_FOLDER

            }catch (e: Exception){
                errorList.add("OUTPUT_GRAPHS_FOLDER")
            }
            try {
                Configs.OUTPUT_PHRASES_FOLDER

            }catch (e: Exception){
                errorList.add("OUTPUT_PHRASES_FOLDER")
            }

            if(errorList.isNotEmpty()){
                throw IllegalArgumentException("some arguments not found: ${errorList.toTypedArray().contentToString()}")
            }
        }
    }
}