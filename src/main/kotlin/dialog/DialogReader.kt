package dialog

import Configs
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import router.RouterProperties
import text.PhraseTextRaw
import java.io.File
import java.io.FileReader
import java.lang.StringBuilder

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

                for (line in it.readLines()) {
                    var exit = false
                    while (!exit) {
                        exit = true
                        when (stepCnt) {
                            0 -> if (line.contains(Configs.DIALOG_READER_HEADER_SEPARATOR)) {
                                rawText.header = line.split(Configs.DIALOG_READER_HEADER_SEPARATOR)[1].trim()
                                stepCnt++
                                logger.info("find header $rawText.header")
                            }
                            1 -> if (line.contains(Configs.DIALOG_READER_MULTIPLY_TEXT_SEPARATOR)) {
                                if(readText.isNotEmpty()) {
                                    texts.add(trimFromBreaks(readText))
                                }
                                readText = line.split(Configs.DIALOG_READER_MULTIPLY_TEXT_SEPARATOR)[1].trim()
                            } else if (line.contains(Configs.DIALOG_READER_ANSWER_SEPARATOR)) {
                                texts.add(trimFromBreaks(readText))
                                logger.info("added text for $rawText.header")
                                rawText.textBody = texts.toTypedArray()
                                texts.clear()
                                readText = ""
                                stepCnt++
                                exit = false
                            } else {
                                readText += line
                            }
                            2 -> if (line.contains(Configs.DIALOG_READER_ANSWER_SEPARATOR)) {
                                answers.add(line.split(Configs.DIALOG_READER_ANSWER_SEPARATOR)[1].trim())
                            } else if (line.contains(Configs.DIALOG_READER_HEADER_SEPARATOR)) {
                                rawText.answers = answers.toTypedArray()
                                logger.info("readed $rawText")
                                list.add(rawText)
                                rawText = PhraseTextRaw()
                                answers.clear()
                                stepCnt = 0
                                exit = false
                            }
                        }
                    }
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


        private fun trimFromBreaks(text: String) : String{
            var res = StringBuilder(text)
            while (res[0] == '\n') res.deleteCharAt(0)
            while (res.last() == '\n') res.deleteCharAt(res.lastIndex)
            return res.toString().trim()
        }

    }

}