package dialog

import Configs
import phraseText.PhraseTextFabric
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter
import models.AnswerType
import models.Indexable
import models.items.text.PhraseText
import models.items.text.PhraseTextStream
import models.router.RouterStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Files.isRegularFile
import java.nio.file.Paths
import kotlin.streams.toList

class DialogCreator {
    companion object{

        public fun createDialogs(folder: String){
            val filesInFolder = Files
                .walk(Paths.get(folder))
                .filter { isRegularFile(it) }
                .map { File(folder, it.fileName.toString())}
                .toList();

            for (file in filesInFolder) {
                val phrases = readPhrasesFromFile(file);
                val graph = createGraph(phrases);

                val routerProperty = DialogReader.readProperty(file);
                val dialogName = routerProperty["id"]

                val phraseFile = File(Configs.OUTPUT_PHRASES_FOLDER, "$dialogName.json")
                val graphFile = File(Configs.OUTPUT_GRAPHS_FOLDER, "$dialogName.graphml")

                PhraseTextStream.write(phrases, phraseFile.absolutePath)
                GraphMLWriter.outputGraph(graph, graphFile.outputStream())
            }

        }

        public fun createGraph(phrases : Array<PhraseText>) : Graph{
            val graph = TinkerGraph();
            val vertexes = hashMapOf<String, Vertex>()
            var cnt = 1;
            for (phrase in phrases) {
                vertexes[phrase.id] = graph.addVertex(cnt++)
                graph.getVertex(vertexes[phrase.id]!!.id).setProperty(Indexable.ID_NAME, phrase.id);
            }

            for (phrase in phrases) {
                for (answer in phrase.answers) {
                    if(answer.type == AnswerType.SIMPLE){
                        graph.addEdge(cnt++, vertexes[phrase.id], vertexes[answer.id], "${vertexes[phrase.id]} -> ${vertexes[answer.id]}")
                    }
                }
            }
            return graph;
        }

        public fun readPhrasesFromFile(file: File) : Array<PhraseText>{
            val res = arrayListOf<PhraseText>()
            for (phraseTextRaw in DialogReader.readPhraseTextRaw(file)) {
                res.add(PhraseTextFabric.create(phraseTextRaw))
            }
            return res.toTypedArray();
        }
    }

}