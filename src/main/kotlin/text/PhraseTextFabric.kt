package text

import Configs
import dialog.DialogCreator
import models.Answer
import models.AnswerType
import models.items.text.PhraseText
import org.apache.commons.lang.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PhraseTextFabric {
    companion object{

        private val logger = LoggerFactory.getLogger(PhraseTextFabric::class.java) as Logger

        private val DEF_CLAZZ = Configs.PHRASE_TEXT_DEF_CLASS

        public fun create(raw : PhraseTextRaw): PhraseText {
            logger.info(">> create from: $raw")
            val phraseBuilder = PhraseTextBuilder()
            processHeader(phraseBuilder, raw)
            processBody(phraseBuilder, raw)
            processAnswers(phraseBuilder, raw)
            val res = phraseBuilder.build()
            logger.info("<< created : $res")
            return res
        }

        private fun  processBody(pb: PhraseTextBuilder, raw: PhraseTextRaw){
            for (text in raw.textBody) {
                pb.addText(text.trim())
            }
        }

        private fun processAnswers(pb: PhraseTextBuilder, raw: PhraseTextRaw){
            // to end (th.end #exit)

            for (answer in raw.answers) {
                var id = StringUtils.substringBetween(answer, "(", ")").trim()
                var type = AnswerType.SIMPLE
                var text = answer.split("(")[0].trim()
                if(id.contains("#")){
                    type = when(id.split("#")[1].trim().toLowerCase()){
                        "exit" -> AnswerType.EXIT
                        "enter" -> AnswerType.ENTER
                        else -> AnswerType.SIMPLE
                    }
                    id = id.split("#")[0].trim()
                }
                pb.addAnswers(Answer(id, text, type))
            }
        }

        private fun processHeader(pb: PhraseTextBuilder, raw: PhraseTextRaw){
            //th.start.1 [models.items.phrase.MultiplyPhrase]
            var header = raw.header.trim()
            var clazz = DEF_CLAZZ
            if(header.contains("[") && header.contains("]") ){
                clazz =  StringUtils.substringBetween(header, "[", "]")
                header = header.split("[")[0].trim()
            }
            val id = header.trim()
            pb.id(id).className(clazz)
        }
    }
}