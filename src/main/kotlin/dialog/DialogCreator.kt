package dialog

import Configs
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import text.PhraseTextFabric
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter
import models.AnswerType
import models.Indexable
import models.items.text.PhraseText
import models.items.text.PhraseTextStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import router.RouterProperties
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Files.isRegularFile
import java.nio.file.Paths
import kotlin.streams.toList

class DialogCreator {
    companion object{

        private val logger = LoggerFactory.getLogger(DialogCreator::class.java) as Logger


        public fun createDialogs(folder: String){
            logger.info(">> createDialogs: $folder")

            val filesInFolder = Files
                .walk(Paths.get(folder), 10)
                .filter { isRegularFile(it) }
                .map { it.toAbsolutePath().toFile() }
                .toList()

            filesInFolder.forEach{ logger.info("found: ${it.name}") }

            logger.info("")
            logger.info("-------- start recognition --------")
            logger.info("")

            val phrasesListMap = hashMapOf<String, ArrayList<PhraseText>>()
            val propertiesListMap = hashMapOf<String, RouterProperties>()

            for(file in filesInFolder) {
                    logger.info("try to parse $file ")
                val routerProperty : RouterProperties
                val phrases : Array<PhraseText>
                try {
                    logger.info("")
                    logger.info("-------- read routers property --------")
                    logger.info("")

                    routerProperty = DialogReader.readProperty(file)

                    logger.info("")
                    logger.info("-------- read phrases --------")
                    logger.info("")

                    phrases = readPhrasesFromFile(file)

                    if (propertiesListMap[routerProperty.id] == null) {
                        propertiesListMap[routerProperty.id] = routerProperty;
                    }
                    val dialogName = routerProperty.id
                    if (phrasesListMap[dialogName] == null) {
                        phrasesListMap[dialogName] = arrayListOf();
                    }
                    logger.info("add to $dialogName")
                    phrasesListMap[dialogName]!!.addAll(phrases);
                } catch (e: Exception) {
                    logger.warn("ERROR, skip: ${e.message}")
                }
            }

            for (dialogId in phrasesListMap.keys) {
                logger.info("")
                logger.info("-------- creating dialog ${phrasesListMap.keys} --------")
                logger.info("")
                try {
                    val phrases = phrasesListMap[dialogId]!!.toTypedArray()
                    logger.info("")
                    logger.info("-------- creating graph --------")
                    logger.info("")
                    val graph = createGraph(phrases)
                    val routerProperty = propertiesListMap[dialogId]!!
                    val routerFile = File(Configs.OUTPUT_ROUTERS_FILE)
                    val phraseFile = File(Configs.OUTPUT_PHRASES_FOLDER, "$dialogId.json")
                    val graphFile = File(Configs.OUTPUT_GRAPHS_FOLDER, "$dialogId.graphml")

                    logger.info("")
                    logger.info("-------- writing --------")
                    logger.info("")

                    logger.info("write ${phrases.toList().map { it.id }.toTypedArray().contentToString()} phrases to ${phraseFile.absolutePath}")
                    PhraseTextStream.write(phrases, phraseFile.absolutePath)

                    logger.info("write graph $graph phrases to ${graphFile.absolutePath}")
                    GraphMLWriter.outputGraph(graph, graphFile.outputStream())

                    logger.info("write router Property $routerProperty phrases to ${routerFile.absolutePath}")
                    writeRoutersToFile(routerFile, routerProperty)
                    logger.info("SUCCESS ")
                } catch (e: Exception) {
                    logger.warn("ERROR, skip: ${e.message}")
                }
            }

            logger.info("---- end recognition")
        }

        public fun createGraph(phrases : Array<PhraseText>) : Graph{
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
                    if(answer.type == AnswerType.SIMPLE){
                        if(vertexes[answer.id] == null){
                            logger.warn("vertex [${answer.id}] not exist , but is in answers ${phrase.id}")
                            logger.warn("add vertex p${answer.id}] in graph, but not in phrases")
                            vertexes[answer.id] = graph.addVertex(cnt++)
                            vertexes[answer.id]!!.setProperty(Indexable.ID_NAME, answer.id)
                        }
                        graph.addEdge(cnt++, vertexes[phrase.id], vertexes[answer.id],
                            "${vertexes[phrase.id]!!.getProperty<String>(Indexable.ID_NAME)} -> " +
                                    "${vertexes[answer.id]!!.getProperty<String>(Indexable.ID_NAME)}")
                        logger.info("added edge [${vertexes[phrase.id]!!.getProperty<String>(Indexable.ID_NAME)} -> " +
                                "${vertexes[answer.id]!!.getProperty<String>(Indexable.ID_NAME)}]")
                    }
                }
            }
            logger.info("<< createGraph: res $graph")
            return graph
        }

        public fun readPhrasesFromFile(file: File) : Array<PhraseText>{
            logger.info(">> readPhrasesFromFile : $file")
            logger.info("")
            val res = arrayListOf<PhraseText>()
            for (phraseTextRaw in DialogReader.readPhraseTextRaw(file)) {
                try {
                    res.add(PhraseTextFabric.create(phraseTextRaw))
                }catch (e: Exception){
                    logger.error("cannot be parsed correctly $phraseTextRaw")
                }
            }
            logger.info("<< readPhrasesFromFile :  read total ${res.size} phrases")
            return res.toTypedArray()
        }

        public fun writeRoutersToFile(file: File, routerProperties: RouterProperties ){
            logger.info(">> writeRoutersToFile : file= $file, routerProperties=$routerProperties" )
            val obj = JsonObject(routerProperties.map())
            var rotersArray = JsonArray<JsonObject>()
            try {
                rotersArray = Klaxon().parseJsonArray(FileReader(file)) as JsonArray<JsonObject>
            }catch (e: Exception){
                if(e is FileNotFoundException)   logger.warn("file not exits! create new" )
                else   logger.warn("Error read file! create new" )
            }

            logger.info("read routers File" )
            logger.info("find ${rotersArray.size} routers" )
            var isContain = false
            for (prop in rotersArray) {
                if(prop["id"] == routerProperties.id) {
                    logger.warn("router ${routerProperties.id} contains in a file: skip" )
                    isContain = true
                    val map = routerProperties.map()
                    for (key in map.keys) {
                        if(key == RouterProperties.idName) continue
                        if(prop[key] != map[key]){
                            logger.warn("property $key in script is differ from routers file  file:${prop[key]} != script:${map[key]}" )
                        }
                    }
                    break
                }
            }
            if(isContain) {
                logger.info("<< writeRoutersToFile: file skipped" )
                return
            }

            logger.info("add routers property to array" )
            rotersArray.add(obj)
            logger.info("write: ${rotersArray.toJsonString()}" )
            FileWriter(file).use{ it.write(rotersArray.toJsonString()) }
            logger.info("<< writeRoutersToFile: OK" )
        }
    }

}