package dialog

import Configs
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import router.RouterProperties
import text.PhraseTextRaw
import tools.StringTool
import java.io.File
import java.io.FileReader
import java.lang.Exception

class DialogReader {

    companion object {

        private val logger = LoggerFactory.getLogger(DialogReader::class.java) as Logger

        public fun readPhraseTextRaw(file: File): Array<PhraseTextRaw> {
             logger.info(">> readPhraseTextRaw from $file")
            val list = ArrayList<PhraseTextRaw>()

            FileReader(file).use {
                var stepCnt = 0 // 0 - header, 1- body, 2-answers
                val texts = arrayListOf<String>()
                val answers = arrayListOf<String>()
                var readText = ""
                var rawText = PhraseTextRaw()
                var cnt = 0;
                for (line in it.readLines()) {
                    var exit = false
                    cnt ++;
                    try {
                        while (!exit) {
                            exit = true
                            when (stepCnt) {
                                0 -> if (line.contains(Configs.DIALOG_READER_HEADER_SEPARATOR)) {
                                    rawText.header = line.split(Configs.DIALOG_READER_HEADER_SEPARATOR)[1].trim()
                                    stepCnt++
                                }
                                1 -> if (line.contains(Configs.DIALOG_READER_MULTIPLY_TEXT_SEPARATOR)) {
                                    prepareAndAddText(readText, texts);
                                    readText = line.split(Configs.DIALOG_READER_MULTIPLY_TEXT_SEPARATOR)[1].trim()

                                } else if (line.contains(Configs.DIALOG_READER_ANSWER_SEPARATOR)) {
                                    prepareAndAddText(readText, texts);
                                    rawText.textBody = texts.toTypedArray()
                                    texts.clear()
                                    readText = ""
                                    stepCnt++
                                    exit = false
                                } else {
                                    readText += "\n"
                                    readText += line
                                }
                                2 -> if (line.contains(Configs.DIALOG_READER_ANSWER_SEPARATOR)) {
                                    answers.add(line.split(Configs.DIALOG_READER_ANSWER_SEPARATOR)[1].trim())
                                } else if (line.contains(Configs.DIALOG_READER_HEADER_SEPARATOR)) {
                                    rawText.answers = answers.toTypedArray()

                                    if(logger.isDebugEnabled) logger.debug("READ: $rawText")
                                    else logger.info("READ: ${rawText.header}")

                                    list.add(rawText)
                                    rawText = PhraseTextRaw()
                                    answers.clear()
                                    stepCnt = 0
                                    exit = false
                                }
                            }
                        }
                    }catch (e : Exception){
                        logger.error("error at line $cnt : $line ($file) skipped")
                        stepCnt = 0;
                    }
                }
                if(answers.isNotEmpty()){
                    rawText.answers = answers.toTypedArray()
                    list.add(rawText)
                    answers.clear()
                }
            }

            logger.info("<< read ${list.size} raw phrase texts")
            return list.toTypedArray()
        }

        public fun readProperty(file: File): RouterProperties{
            logger.info(">> readProperty from $file")

            val map = hashMapOf<String,Any>()
            FileReader(file).use {
                for (line in it.readLines()) {
                    if(line.contains(Configs.DIALOG_READER_ROUTER_PROPERTY_SEPARATOR)){
                        val arr = line.split(Configs.DIALOG_READER_ROUTER_PROPERTY_SEPARATOR)[1].trim().split("=")
                        if(arr[0] == "isResetToStart"){
                            map[arr[0]] = arr[1].toBoolean()
                        }else{
                            map[arr[0]] = arr[1].trim()
                        }
                    }
                    if(line.contains(Configs.DIALOG_READER_HEADER_SEPARATOR)){
                        val res = RouterProperties(map)
                        logger.info(">> read Property $res")
                        return res

                    }
                }
            }
            logger.error(">> id is not found")
            throw IllegalAccessException("id is not found")
        }


        private fun prepareAndAddText(text: String, list: ArrayList<String>) {
            val res =  StringTool.fixText(text);
            if(res.isNotEmpty()) {
                list.add(res)
            }
        }

    }

}