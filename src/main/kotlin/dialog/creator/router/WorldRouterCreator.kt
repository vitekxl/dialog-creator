package dialog.creator.router

import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter
import dialog.creator.dialog.DialogCreator
import dialog.system.models.AnswerType
import dialog.system.models.Indexable
import dialog.system.models.items.text.PhraseText
import dialog.system.models.items.text.PhraseTextStream
import dialog.system.models.router.Router
import dialog.system.models.router.RouterStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList

class WorldRouterCreator {



    companion object {
        private val logger = LoggerFactory.getLogger(WorldRouterCreator::class.java) as Logger


        public fun create(routerProperties: RouterProperties, pathToRouter: String, pathToGraphs: String, pathToPhrases: String) : Router {
            logStep("start create world router")
            val routers = RouterStream.readMany(pathToRouter, pathToGraphs)
            val dialogsId = routers.map { it.id }
            val phrases = arrayListOf<PhraseText>()
            // val exitEnterAnswerMap<String, String>

            dialogsId.forEach{
                logger.info("Dialog found: $it")
            }

            logStep("reading phrases")

            Files
                .walk(Paths.get(pathToPhrases))
                .filter { Files.isRegularFile(it)}
                .map { File(pathToPhrases, it.fileName.toString()).absolutePath }
                .forEach {
                    logger.info("read $it")
                    phrases.addAll(PhraseTextStream.readMany(it)!!)
                }

            val exitEnterPhrases = phrases
                .filter { phrase -> phrase.answers.any { answer -> answer.type != AnswerType.SIMPLE } }
                .map {
                    PhraseText(
                        it.id,
                        arrayOf(""),
                        it.answers.filter { answer -> answer.type != AnswerType.SIMPLE }.toTypedArray()
                    )
                }

            val dialogsIdEdges  = arrayListOf<Pair<String,String>>()

            var cnt =0;
            exitEnterPhrases.forEach{ phraseText ->
                val fromDialog = getRouterIdContainedPhase(routers, phraseText.id)
                if(fromDialog == null){
                    logger.warn("${phraseText.id} not found")
                    return@forEach
                }
                phraseText.answers.forEach {
                    if(dialogsId.contains(it.id)) dialogsIdEdges.add(Pair(fromDialog,it.id))
                    else{
                        logger.warn("$fromDialog->${it.id} :  ${it.id} not found")
                    }
                }
                cnt ++;
            }


            val graph =  createGraphFromEdges(dialogsIdEdges);
            val router = Router(routerProperties.id, graph, hashMapOf(), routerProperties.startPointId, routerProperties.isResetToStart)
            return router;
        }

        public fun createAndWrite(routerProperties: RouterProperties, pathToRouter: String, pathToGraphs: String, pathToPhrases: String){
           logStep("create and write world graph")
            val routerworld = create(routerProperties, pathToRouter, pathToGraphs, pathToPhrases)
            logStep("writing world graph")
            DialogCreator().writeRouterPropertiesToFile(arrayListOf(routerProperties), pathToRouter)
            val graphFile = File(pathToGraphs, "${routerworld.id}.graphml")
            logger.info("write world router graph ${graphFile.absoluteFile}")
            GraphMLWriter.outputGraph(routerworld.graph, graphFile.outputStream())
        }

        private fun getRouterIdContainedPhase(routers: Array<Router>, itemId: String) : String?{
            routers.forEach {
                if(graphContainVertexWithProperty(it.graph, itemId)) return it.id;
            }
            return null;
        }

        private fun graphContainVertexWithProperty(graph: Graph, itemId: String) : Boolean{
            graph.vertices.forEach{
                if (it.getProperty<String>(Indexable.ID_NAME) == itemId) return true;
            }
            return false
        }



        private fun createGraphFromEdges(map : List<Pair<String, String>>) : Graph{
            val graph = TinkerGraph()
            val vertexes = hashMapOf<String, Vertex>()
            var cnt = 1
            for (pair in map ) {
                for (i in 0..1) {
                    val dialogsId = if(i==0) pair.first else pair.second

                    if (vertexes.contains(dialogsId)) continue;
                    vertexes[dialogsId] = graph.addVertex(cnt++)
                    graph.getVertex(vertexes[dialogsId]!!.id).setProperty(Indexable.ID_NAME, dialogsId)
                    logger.info("added vertex [${dialogsId}]")
                }
            }

            for (edge in map) {

                graph.addEdge(cnt++, vertexes[edge.first], vertexes[edge.second],
                    "${vertexes[edge.first]!!.getProperty<String>(Indexable.ID_NAME)} -> " +
                            "${vertexes[edge.second]!!.getProperty<String>(Indexable.ID_NAME)}")


                logger.info("added edge [${vertexes[edge.first]!!.getProperty<String>(Indexable.ID_NAME)} -> " +
                        "${vertexes[edge.second]!!.getProperty<String>(Indexable.ID_NAME)}]")
            }
            return graph
        }
        private fun logStep(stepName: String) {
           logger.info("")
            logger.info("-------- $stepName --------")
            logger.info("")
        }
    }

}
