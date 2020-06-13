package dialog.creator.parser

import dialog.creator.Configs
import dialog.creator.text.PhraseTextRaw
import dialog.creator.tools.LogUtil
import dialog.creator.tools.StringTool
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader

class PhraseTextRawParser {

    companion object {
        private val logger = LoggerFactory.getLogger(PhraseTextRawParser::class.java) as Logger
        private val logUtil = LogUtil(logger)


        public fun parseRaw(file: File): Array<PhraseTextRaw> {
            logger.info(">> readPhraseTextRaw from $file")
            val list = ArrayList<PhraseTextRaw>()

            FileReader(file).use {
                var stepCnt = 0 // 0 - header, 1- body, 2-answers
                val texts = arrayListOf<String>()
                val answers = arrayListOf<String>()
                var readText = ""
                var rawText = PhraseTextRaw()
                var cnt = 0;
                for (_line in it.readLines()) {
                    val line = _line.trim();
                    if (line.startsWith("//")) continue; // comments
                    var exit = false
                    cnt++;
                    try {
                        while (!exit) {
                            exit = true
                            when (stepCnt) {
                                0 -> if (line.startsWith(Configs.DIALOG_READER_HEADER_SEPARATOR)) {
                                    rawText.header = line.substringAfter(Configs.DIALOG_READER_HEADER_SEPARATOR).trim()
                                    stepCnt++
                                }
                                1 -> if (line.startsWith(Configs.DIALOG_READER_MULTIPLY_TEXT_SEPARATOR)) {
                                    prepareAndAddText(
                                        readText,
                                        texts
                                    );
                                    readText = line.substringAfter(Configs.DIALOG_READER_MULTIPLY_TEXT_SEPARATOR).trim()

                                } else if (line.startsWith(Configs.DIALOG_READER_ANSWER_SEPARATOR)) {
                                    prepareAndAddText(
                                        readText,
                                        texts
                                    );
                                    rawText.textBody = texts.toTypedArray()
                                    texts.clear()
                                    readText = ""
                                    stepCnt++
                                    exit = false
                                } else {
                                    readText += "\n"
                                    readText += line
                                }
                                2 -> if (line.startsWith(Configs.DIALOG_READER_ANSWER_SEPARATOR)) {
                                    answers.add(line.substringAfter(Configs.DIALOG_READER_ANSWER_SEPARATOR).trim())
                                } else if (line.startsWith(Configs.DIALOG_READER_HEADER_SEPARATOR)) {
                                    rawText.answers = answers.toTypedArray()

                                    if (logger.isDebugEnabled) logger.debug("READ: $rawText")
                                    else logger.info("READ: ${rawText.header}")

                                    list.add(rawText)
                                    rawText = PhraseTextRaw()
                                    answers.clear()
                                    stepCnt = 0
                                    exit = false
                                }
                            }
                        }
                    } catch (e: java.lang.Exception) {
                        logUtil.logError("error at line $cnt : $line ($file) skipped")
                        stepCnt = 0;
                    }
                }
                if (answers.isNotEmpty()) {
                    rawText.answers = answers.toTypedArray()
                    list.add(rawText)
                    if (logger.isDebugEnabled) logger.debug("READ: $rawText")
                    else logger.info("READ: ${rawText.header}")
                    answers.clear()
                }
            }

            logger.info("<< read ${list.size} raw phrase texts")
            return list.toTypedArray()
        }

        private fun prepareAndAddText(text: String, list: ArrayList<String>) {
            val res =  StringTool.fixText(text);
            if(res.isNotEmpty()) {
                list.add(res)
            }
        }
    }

}