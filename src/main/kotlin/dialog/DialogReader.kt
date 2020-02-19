package dialog

import Configs
import phraseText.PhraseTextRaw
import java.io.File
import java.io.FileReader
import java.lang.StringBuilder

class DialogReader {

    companion object {

        public fun readPhraseTextRaw(file: File): Array<PhraseTextRaw> {

            val list = ArrayList<PhraseTextRaw>()

            FileReader(file).use {
                var stepCnt = 0; // 0 - header, 1- body, 2-answers
                val texts = arrayListOf<String>()
                val answers = arrayListOf<String>()
                var readText = "";
                var rawText = PhraseTextRaw()

                for (line in it.readLines()) {
                    var exit = false;
                    while (!exit) {
                        exit = true;
                        when (stepCnt) {
                            0 -> if (line.contains(Configs.DIALOG_READER_HEADER_SEPARATOR)) {
                                rawText.header = line.split(Configs.DIALOG_READER_HEADER_SEPARATOR)[1].trim()
                                stepCnt++;
                            }
                            1 -> if (line.contains(Configs.DIALOG_READER_MULTIPLY_TEXT_SEPARATOR) && readText.isNotEmpty()) {
                                texts.add(trimFromBreaks(readText));
                                readText = line.split(Configs.DIALOG_READER_MULTIPLY_TEXT_SEPARATOR)[1].trim();
                            } else if (line.contains(Configs.DIALOG_READER_ANSWER_SEPARATOR)) {
                                texts.add(trimFromBreaks(readText));
                                rawText.textBody = texts.toTypedArray();
                                texts.clear();
                                readText = "";
                                stepCnt++;
                                exit = false;
                            } else {
                                readText += line;
                            }
                            2 -> if (line.contains(Configs.DIALOG_READER_ANSWER_SEPARATOR)) {
                                answers.add(line.split(Configs.DIALOG_READER_ANSWER_SEPARATOR)[1].trim())
                            } else if (line.contains(Configs.DIALOG_READER_HEADER_SEPARATOR)) {
                                rawText.answers = answers.toTypedArray()
                                list.add(rawText);
                                rawText = PhraseTextRaw()
                                answers.clear()
                                stepCnt = 0
                                exit = false
                            }
                        }
                    }
                }
            }
            return list.toTypedArray();
        }

        public fun readProperty(file: File): HashMap<String,Any>{

            val res = hashMapOf<String,Any>()
            FileReader(file).use {
                for (line in it.readLines()) {
                    if(line.contains(Configs.DIALOG_READER_ROUTER_PROPERTY_SEPARATOR)){
                        val arr = line.split(Configs.DIALOG_READER_ROUTER_PROPERTY_SEPARATOR)[1].trim().split("=")
                        if(arr[0] == "isResetToStart"){
                            res[arr[0]] = arr[1].toBoolean();
                        }else{
                            res[arr[0]] = arr[1].trim();
                        }
                    }
                    if(line.contains(Configs.DIALOG_READER_HEADER_SEPARATOR)){
                            return res
                    }
                }
            }
            throw IllegalAccessException("name is not found");
        }


        private fun trimFromBreaks(text: String) : String{
            var res = StringBuilder(text);
            while (res[0] == '\n') res.deleteCharAt(0);
            while (res.last() == '\n') res.deleteCharAt(res.lastIndex);
            return res.toString().trim();
        }
    }

}