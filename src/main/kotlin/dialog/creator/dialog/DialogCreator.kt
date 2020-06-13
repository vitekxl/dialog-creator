package dialog.creator.dialog

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter
import dialog.creator.Configs
import dialog.creator.parser.DialogParser
import dialog.creator.parser.PhraseTextParser
import dialog.creator.router.RouterProperties
import dialog.creator.tools.LogUtil
import dialog.creator.tools.StaticList
import dialog.system.io.PhraseTextStream
import dialog.system.models.Indexable
import dialog.system.models.answer.AnswerType
import dialog.system.models.items.text.PhraseText
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
        private val logUtil = LogUtil(logger)
    }

    private val errorListMessages = StaticList.list

    public fun createAndWriteDialogs(folder: String) : Boolean {
        logger.info(">> createDialogs: $folder")

        val filesInFolder = Files
            .walk(Paths.get(folder), 10)
            .filter { isRegularFile(it) }
            .map { it.toAbsolutePath().toFile() }
            .toList()

        filesInFolder.forEach { logger.info("found: ${it.name}") }

        logUtil.logStep("start recognition")

        val phrasesListMap: HashMap<String, ArrayList<PhraseText>> = PhraseTextParser.readPhraseTextsLists(filesInFolder)
        val propertiesMap: HashMap<String, RouterProperties> = readRouterProperties(filesInFolder)

        writePhrases(phrasesListMap, outputPhrasesFolder)
        writeRouterPropertiesToFile(propertiesMap.values, outputRoutersFile, false)
        writeGraphs(phrasesListMap, outputGraphsFolder)

        if (errorListMessages.isNotEmpty()) {
            logger.error("Dialog Creation failed with errors: ${errorListMessages.size}")
            errorListMessages.forEach { logger.error(it) }
        }

        logUtil.logStep("end recognition")
        return  errorListMessages.isEmpty()
    }


    private fun writePhrases(phrasesListMap: HashMap<String, ArrayList<PhraseText>>, outputPhrasesFolder: String) {
        logUtil.logStep("write phrases")
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
                logUtil.logError( "ERROR by writing phrase $dialogId: ${e.message}")
            }
        }
        logger.info("SUCCESS")
    }


    private fun writeGraphs(phrasesListMap: HashMap<String, ArrayList<PhraseText>>, outputGraphsFolder: String) {
        logUtil.logStep( "Write Graphs")

        for (dialogId in phrasesListMap.keys) {
            logUtil.logStep("creating dialo ${dialogId}")
            try {
                val phrases = phrasesListMap[dialogId]!!.toTypedArray()
                val graph = createGraph(phrases)
                val graphFile = File(outputGraphsFolder, "$dialogId.graphml")
                logger.info("write graph $graph phrases to ${graphFile.absolutePath}")
                GraphMLWriter.outputGraph(graph, graphFile.outputStream())

            } catch (e: Exception) {
                logUtil.logError("ERROR by writing Graph ${dialogId}: ${e.message}")
            }
        }
        logger.info("SUCCESS")
    }


    private fun readRouterProperties(filesInFolder: List<File>): HashMap<String, RouterProperties> {
        logUtil.logStep( "read routers property")
        val propertiesMap = HashMap<String, RouterProperties>();

        for (file in filesInFolder) {
            val routerProperty: RouterProperties

            try {
                routerProperty = DialogParser.readProperty(file)
                if (propertiesMap[routerProperty.id] == null) {
                    propertiesMap[routerProperty.id] = routerProperty;
                }
            } catch (e: Exception) {
                logUtil.logError( "ERROR by reading router property from $file, skip: ${e.message}")
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




    public fun writeRouterPropertiesToFile(
        routerProperties: Collection<RouterProperties>,
        filePath: String,
        isOverwrite: Boolean = false
    ) {
        logUtil.logStep( "Write Router Properties")
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



}
