package dialog.creator.parser

import dialog.creator.router.RouterProperties
import dialog.creator.text.PhraseTextFabric
import dialog.creator.tools.LogUtil
import dialog.system.models.items.text.PhraseText
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class PhraseTextParser {

    companion object {
        private val logger = LoggerFactory.getLogger(PhraseTextParser::class.java) as Logger
        private val logUtil = LogUtil(logger)

        public fun parse(file: File): Array<PhraseText> {
            logger.info(">> readPhrasesFromFile : ${file.name}")
            logger.info("")
            val res = arrayListOf<PhraseText>()
            for (phraseTextRaw in PhraseTextRawParser.parseRaw(file)) {
                try {
                    res.add(PhraseTextFabric.create(phraseTextRaw))
                } catch (e: Exception) {
                    // logError("cannot be parsed correctly : ${e.message} \n$phraseTextRaw")
                    logger.error("cannot be parsed correctly : ${e.message} \n$phraseTextRaw")
                }
            }
            logger.info("<< readPhrasesFromFile : read total ${res.size} phrases")
            return res.toTypedArray()
        }


        public fun readPhraseTextsLists(filesInFolder: List<File>): HashMap<String, ArrayList<PhraseText>> {
            logUtil.logStep("read phrases text")
            val phrasesListMap: HashMap<String, ArrayList<PhraseText>> = hashMapOf()

            for (file in filesInFolder) {
                val routerProperty: RouterProperties
                val phrases: Array<PhraseText>

                try {
                    routerProperty = DialogParser.readProperty(file)
                    phrases = PhraseTextParser.parse(file)

                    val dialogName = routerProperty.id
                    if (phrasesListMap[dialogName] == null) {
                        phrasesListMap[dialogName] = arrayListOf();
                    }

                    logger.info(
                        "add ${phrases.map { it.id }.toTypedArray().contentToString()} to $dialogName"
                    )
                    phrasesListMap[dialogName]!!.addAll(phrases);
                } catch (e: Exception) {
                    logUtil.logError("ERROR by reading router text from $file, skip: ${e.message}")
                }
            }
            return phrasesListMap;
        }
    }
}