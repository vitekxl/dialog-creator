package dialog.creator

import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter
import dialog.creator.dialog.DialogCreator
import dialog.creator.router.RouterProperties
import dialog.creator.router.WorldRouterCreator
import java.io.File


//-og
//"/Users/viktor.bilousov/Library/Mobile Documents/com~apple~CloudDocs/JavaProjects/dialog-creator/src/main/resources/graphs"
//-op
//"/Users/viktor.bilousov/Library/Mobile Documents/com~apple~CloudDocs/JavaProjects/dialog-creator/src/main/resources/phrases"

class CreateWorldRouter {
    companion object{
        @JvmStatic
        fun main(args: Array<String>) {
            val router = "/Users/viktor.bilousov/Library/Mobile Documents/com~apple~CloudDocs/JavaProjects/dialog-creator/src/main/resources/routers/routers.json"
            val graphs = "/Users/viktor.bilousov/Library/Mobile Documents/com~apple~CloudDocs/JavaProjects/dialog-creator/src/main/resources/graphs"
            val phrases = "/Users/viktor.bilousov/Library/Mobile Documents/com~apple~CloudDocs/JavaProjects/dialog-creator/src/main/resources/phrases"
            val routerProperties = RouterProperties("router.world.text", true, "world")
            val routerworld = WorldRouterCreator.create(routerProperties, router, graphs, phrases)
            DialogCreator().writeRouterPropertiesToFile(arrayListOf(routerProperties), router)
            val graphFile = File(graphs, "${routerworld.id}.graphml")
            GraphMLWriter.outputGraph(routerworld.graph.graph, graphFile.outputStream())

        }
    }
}