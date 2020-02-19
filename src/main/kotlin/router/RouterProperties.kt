package router

data class RouterProperties(var id : String, var isResetToStart : Boolean, var startPointId: String){

    companion object{
        public const val IsResetToStartName = "isResetToStart"
        public const val idName = "id"
        public const val startPointIdName = "startPointId"
    }

    public fun map(): HashMap<String, Any> {
        return hashMapOf(
            Pair("id", id),
            Pair("isResetToStart", isResetToStart),
            Pair("startPointId", startPointId)
        )
    }
    constructor(map: HashMap<String, Any>) : this(map["id"] as String, map["isResetToStart"] as Boolean, map["startPointId"] as String )
}