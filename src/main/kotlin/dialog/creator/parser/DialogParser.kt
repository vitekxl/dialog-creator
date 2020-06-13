package dialog.creator.parser

import dialog.creator.Configs
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import dialog.creator.router.RouterProperties
import dialog.creator.text.PhraseTextRaw
import dialog.creator.tools.LogUtil
import dialog.creator.tools.StringTool
import java.io.File
import java.io.FileReader
import java.lang.Exception

class DialogParser {

    companion object {

        private val logger = LoggerFactory.getLogger(DialogParser::class.java) as Logger
        private val logUtil = LogUtil(logger)


        public fun readProperty(file: File): RouterProperties {
            logger.info("")
            logger.info("------> read Property from ${file.name}")

            val map = hashMapOf<String,Any>()
            FileReader(file).use {
                for (line in it.readLines()) {
                    if(line.startsWith(Configs.DIALOG_READER_ROUTER_PROPERTY_SEPARATOR)){
                        val arr = line.substringAfter(Configs.DIALOG_READER_ROUTER_PROPERTY_SEPARATOR).trim().split("=")
                        if(arr[0] == "isResetToStart"){
                            map[arr[0]] = arr[1].toBoolean()
                        }else{
                            map[arr[0]] = arr[1].trim()
                        }
                    }
                    if(line.contains(Configs.DIALOG_READER_HEADER_SEPARATOR)){
                        val res = RouterProperties(map)
                        logger.info("found Property $res")
                        return res

                    }
                }
            }
            logUtil.logError(">> id is not found")
            throw IllegalAccessException("id is not found")
        }



    }

}