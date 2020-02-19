import java.lang.IllegalArgumentException

class Configs {
    companion object{
        public var PHRASE_TEXT_DEF_CLASS  : String = "NAN"
            get() {
                if(field  == "NAN"){
                    return "models.items.phrase.FilteredPhrase"
                }
                return field
            }

        public var INPUT_SCRIPT_FOLDER : String = "NAN"
            get() {
                if(field == "NAN"){
                    throw IllegalArgumentException("INPUT FOLDER IS NOT SET")
                }
                return field
            }

        public var OUTPUT_PHRASES_FOLDER : String = "NAN"
            get() {
                if(field  == "NAN"){
                    throw IllegalArgumentException("OUTPUT FOLDER IS NOT SET")
                }
                return field
            }

        public var OUTPUT_GRAPHS_FOLDER : String = "NAN"
            get() {
                if(field == "NAN"){
                    throw IllegalArgumentException("OUTPUT FOLDER IS NOT SET")
                }
                return field
            }

        public var OUTPUT_ROUTERS_FILE : String = "NAN"
            get() {
                if(field == "NAN"){
                    throw IllegalArgumentException("ROUTERS FOLDER IS NOT SET")
                }
                return field
            }

        public const val DIALOG_READER_HEADER_SEPARATOR  : String = "----"
        public const val DIALOG_READER_MULTIPLY_TEXT_SEPARATOR  : String = "##"
        public const val DIALOG_READER_ROUTER_PROPERTY_SEPARATOR  : String = "#"
        public const val DIALOG_READER_ANSWER_SEPARATOR  : String = ">"

    }


}