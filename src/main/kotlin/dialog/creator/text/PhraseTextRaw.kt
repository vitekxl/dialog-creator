package dialog.creator.text

data class PhraseTextRaw(var header: String, var textBody: Array<String>, var answers: Array<String>) {


    constructor() : this("NAN", emptyArray<String>(), emptyArray<String>())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PhraseTextRaw

        if (header != other.header) return false
        if (!textBody.contentEquals(other.textBody)) return false
        if (!answers.contentEquals(other.answers)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + textBody.contentHashCode()
        result = 31 * result + answers.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "PhraseTextRaw={header=$header\ntextBody=${textBody
            .map { it.replace("\n", " '\\n' ") }
            .joinToString("}, { ", "[{","}]")}" +
                " \nanswers=${answers.joinToString("}, {", "[{","}]")}"
    }

}