package dialog.creator.text

import dialog.creator.Configs
import dialog.system.models.answer.Answer
import dialog.system.models.answer.AnswerType
import dialog.system.models.items.text.PhraseText
import org.apache.commons.lang.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception

class PhraseTextFabric {
    companion object{

        private val logger = LoggerFactory.getLogger(PhraseTextFabric::class.java) as Logger

        private val DEF_CLAZZ = Configs.PHRASE_TEXT_DEF_CLASS

        public fun create(raw : PhraseTextRaw): PhraseText {
            val phraseBuilder = PhraseTextBuilder()
            var headerOK= false;
            var bodyOK= false;
            var answerOk= false;
            try {
                processHeader(phraseBuilder, raw)
                headerOK = true;
                processBody(phraseBuilder, raw)
                bodyOK = true
                processAnswers(phraseBuilder, raw)
                answerOk = true
            }catch (e: Exception){
                if (!headerOK) logger.error("error by creating header: ${e.message}")
                else if (!bodyOK) logger.error("error by creating body: ${e.message}")
                else if (!answerOk) logger.error("error by creating answer: ${e.message}")
                throw e
            }
            val res = phraseBuilder.build()
            if(logger.isDebugEnabled) logger.debug("CREATED: $res")
            else logger.info("CREATED: ${res.id}")
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