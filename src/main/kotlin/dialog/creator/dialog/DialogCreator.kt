package dialog.creator.dialog

import dialog.creator.Configs
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import dialog.creator.text.PhraseTextFabric
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter
import dialog.system.models.AnswerType
import dialog.system.models.Indexable
import dialog.system.models.items.text.PhraseText
import dialog.system.models.items.text.PhraseTextStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import dialog.creator.router.RouterProperties
import dialog.creator.text.PhraseTextRaw
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Files.isRegularFile
import java.nio.file.Paths
import kotlin.streams.toList

class DialogCreator(
    private val outputPhrasesFolder: String = Configs.OUTPUT_PHRASES_FOLDER,
    private val outputRoutersFile: String = Configs.OUTPUT_ROUTERS_FILE,
    private val outputGraphsFolder: String = Configs.OUTPUT_PHRASES_FOLDER
) {
    companion object {

        private val logger = LoggerFactory.getLogger(DialogCreator::class.java) as Logger
    }

    private val errorListMessages = arrayListOf<String>()

    public fun createAndWriteDialogs(folder: String ) : Boolean {
        logger.info(">> createDialogs: $folder")

        val filesInFolder = Files
            .walk(Paths.get(folder), 10)
            .filter { isRegularFile(it) }
            .map { it.toAbsolutePath().toFile() }
            .toList()

        filesInFolder.forEach { logger.info("found: ${it.name}") }

        logStep("start recognition")

        val phrasesListMap: HashMap<String, ArrayList<PhraseText>> = readPhraseTextsLists(filesInFolder)
        val propertiesMap: HashMap<String, RouterProperties> = readRouterProperties(filesInFolder)

        writePhrases(phrasesListMap, outputPhrasesFolder)
        writeRouterPropertiesToFile(propertiesMap.values, outputRoutersFile, false)
        writeGraphs(phrasesListMap, outputGraphsFolder)

        if (errorListMessages.isNotEmpty()) {
            logger.error("Dialog Creation failed with errors: ${errorListMessages.size}")
            errorListMessages.forEach { logger.error(it) }
        }

        logStep("end recognition")
        return  errorListMessages.isEmpty()
    }


    private fun writePhrases(phrasesListMap: HashMap<String, ArrayList<PhraseText>>, outputPhrasesFolder: String) {
        logStep("write phrases")
        for (dialogId in phrasesListMap.keys) {
            try {
                val phrases = phrasesListMap[dialogId]!!.toTypedArray()
                val phraseFile = File(outputPhrasesFolder, "$dialogId.json")
                logger.info(
                    "write ${phrases.toList().map { it.id }.toTypedArray()
                        .contentToString()} phrases to ${phraseFile.absolutePath}"
                )
                PhraseTextStream.write(phrases, phraseFile.absolutePath)
            } catch (e: Exception) {
                logError("ERROR by writing phrase $dialogId: ${e.message}")
            }
        }
        logger.info("SUCCESS")
    }


    private fun writeGraphs(phrasesListMap: HashMap<String, ArrayList<PhraseText>>, outputGraphsFolder: String) {
        logStep("Write Graphs")

        for (dialogId in phrasesListMap.keys) {
            logStep("creating dialo ${dialogId}")
            try {
                val phrases = phrasesListMap[dialogId]!!.toTypedArray()
                val graph = createGraph(phrases)
                val graphFile = File(outputGraphsFolder, "$dialogId.graphml")
                logger.info("write graph $graph phrases to ${graphFile.absolutePath}")
                GraphMLWriter.outputGraph(graph, graphFile.outputStream())

            } catch (e: Exception) {
                logError("ERROR by writing Graph ${dialogId}: ${e.message}")
            }
        }
        logger.info("SUCCESS")
    }


    private fun readPhraseTextsLists(filesInFolder: List<File>): HashMap<String, ArrayList<PhraseText>> {
        logStep("read phrases text")
        val phrasesListMap: HashMap<String, ArrayList<PhraseText>> = hashMapOf()

        for (file in filesInFolder) {
            val routerProperty: RouterProperties
            val phrases: Array<PhraseText>

            try {
                routerProperty = DialogReader.readProperty(file)
                phrases = readPhrasesFromFile(file)

                val dialogName = routerProperty.id
                if (phrasesListMap[dialogName] == null) {
                    phrasesListMap[dialogName] = arrayListOf();
                }

                logger.info("add ${phrases.map { it.id }.toTypedArray().contentToString()} to $dialogName")
                phrasesListMap[dialogName]!!.addAll(phrases);
            } catch (e: Exception) {
                logStep("ERROR by reading router text from $file, skip: ${e.message}")
            }
        }
        return phrasesListMap;
    }

    private fun readRouterProperties(filesInFolder: List<File>): HashMap<String, RouterProperties> {
        logStep("read routers property")
        val propertiesMap = HashMap<String, RouterProperties>();

        for (file in filesInFolder) {
            val routerProperty: RouterProperties

            try {
                routerProperty = DialogReader.readProperty(file)
                if (propertiesMap[routerProperty.id] == null) {
                    propertiesMap[routerProperty.id] = routerProperty;
                }
            } catch (e: Exception) {
                logError("ERROR by reading router property from $file, skip: ${e.message}")
            }
        }
        return propertiesMap;
    }


    public fun createGraph(phrases: Array<PhraseText>): Graph {
        logger.info(">>createGraph")
        val graph = TinkerGraph()
        val vertexes = hashMapOf<String, Vertex>()
        var cnt = 1
        for (phrase in phrases) {
            vertexes[phrase.id] = graph.addVertex(cnt++)
            graph.getVertex(vertexes[phrase.id]!!.id).setProperty(Indexable.ID_NAME, phrase.id)
            logger.info("added vertex [${phrase.id}]")
        }

        for (phrase in phrases) {
            for (answer in phrase.answers) {
                if (answer.type == AnswerType.SIMPLE) {
                    if (vertexes[answer.id] == null) {
                        logger.warn("vertex [${answer.id}] not exist , but is in answers ${phrase.id}")
                        logger.warn("add vertex p${answer.id}] in graph, but not in phrases")
                        vertexes[answer.id] = graph.addVertex(cnt++)
                        vertexes[answer.id]!!.setProperty(Indexable.ID_NAME, answer.id)
                    }
                    graph.addEdge(
                        cnt++, vertexes[phrase.id], vertexes[answer.id],
                        "${vertexes[phrase.id]!!.getProperty<String>(Indexable.ID_NAME)} -> " +
                                "${vertexes[answer.id]!!.getProperty<String>(Indexable.ID_NAME)}"
                    )
                    logger.info(
                        "added edge [${vertexes[phrase.id]!!.getProperty<String>(Indexable.ID_NAME)} -> " +
                                "${vertexes[answer.id]!!.getProperty<String>(Indexable.ID_NAME)}]"
                    )
                }
            }
        }
        logger.info("<< createGraph: res $graph")
        return graph
    }

    public fun readPhrasesFromFile(file: File): Array<PhraseText> {
        logger.info(">> readPhrasesFromFile : ${file.name}")
        logger.info("")
        val res = arrayListOf<PhraseText>()
        for (phraseTextRaw in DialogReader.readPhraseTextRaw(file)) {
            try {
                res.add(PhraseTextFabric.create(phraseTextRaw))
            } catch (e: Exception) {
                logError("cannot be parsed correctly : ${e.message} \n$phraseTextRaw")
            }
        }
        logger.info("<< readPhrasesFromFile : read total ${res.size} phrases")
        return res.toTypedArray()
    }


    public fun writeRouterPropertiesToFile(
        routerProperties: Collection<RouterProperties>,
        filePath: String,
        isOverwrite: Boolean = false
    ) {
        logStep("Write Router Properties")
        val file = File(filePath)
        //routers file could already exist, and its needed to add new routers to this file
        var routersArray = JsonArray<JsonObject>()
        try {
            routersArray = Klaxon().parseJsonArray(FileReader(file)) as JsonArray<JsonObject>
        } catch (e: Exception) {
            if (e is FileNotFoundException) logger.info("file not exits! create new")
            else logger.warn("Error read file! create new")
        }

        logger.info("find ${routersArray.size} routers")


        val array = JsonArray<JsonObject>();
        array.addAll(routerProperties.map { JsonObject(it.map()) })

        if (routersArray.isEmpty()) {
            routersArray = array;
        } else {
            array.forEach {
                val id = it[RouterProperties.idName].toString()
                if (!jsonArrayContainsById(id, routersArray)) {
                    routersArray.add(it)
                    logger.info("add router: ${it.toJsonString()}")
                } else if (isOverwrite) {
                    logger.warn("router ${it.toJsonString()}  is already contained in a file: overwritten")
                    removeFromArrayById(id, routersArray)
                    routersArray.add(it)
                    logger.info("add router: ${it.toJsonString()}")
                } else {
                    logger.warn("router ${it.toJsonString()} is already contained in a file: skipped")
                }
            }
        }

        logger.info("write routers to file $file: \n ${routersArray.map { it["id"] }}")
        FileWriter(file).use { it.write(routersArray.toJsonString()) }
        logger.info("<< writeRoutersToFile: OK")
    }

    private fun jsonArrayContainsById(id: String, array: JsonArray<JsonObject>): Boolean {
        array.forEach {
            if (it[RouterProperties.idName] == id) return true
        }
        return false
    }

    private fun removeFromArrayById(id: String, array: JsonArray<JsonObject>): Boolean {
        array.forEachIndexed { index, jsonObject ->
            if (jsonObject[RouterProperties.idName] == id) {
                array.removeAt(index)
                return true
            }
        }
        return false
    }


    private fun logStep(stepName: String) {
        logger.info("")
        logger.info("-------- $stepName --------")
        logger.info("")
    }

    private fun logError(msg: String) {
        logger.warn(msg)
        errorListMessages.add(msg)
    }
}
