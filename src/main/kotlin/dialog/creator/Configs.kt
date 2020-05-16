package dialog.creator

class Configs {
    companion object{


        public const val VERSION: String = "1.3"

        public lateinit var ROUTER_START_POINT: String ;

        public var CREATE_WORLD_ROUTER: Boolean  = false
        public var WORLD_ROUTER_RESET : Boolean = true;
        public lateinit var WORLD_ROUTER_NAME: String

        public var PHRASE_TEXT_DEF_CLASS  : String = "models.items.phrase.FilteredPhrase"
        public lateinit var INPUT_SCRIPT_FOLDER : String
        public lateinit var OUTPUT_PHRASES_FOLDER : String
        public lateinit var OUTPUT_GRAPHS_FOLDER : String
        public lateinit var OUTPUT_ROUTERS_FILE : String

        public const val DIALOG_READER_HEADER_SEPARATOR  : String = "----"
        public const val DIALOG_READER_MULTIPLY_TEXT_SEPARATOR  : String = "@"
        public const val DIALOG_READER_ROUTER_PROPERTY_SEPARATOR  : String = "$"
        public const val DIALOG_READER_ANSWER_SEPARATOR  : String = ">"


    }


}