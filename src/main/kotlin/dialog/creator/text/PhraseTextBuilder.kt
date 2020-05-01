package dialog.creator.text

import models.Answer
import models.items.text.PhraseText
import java.lang.IllegalStateException

class PhraseTextBuilder() {

    var id : String? = null
        private set
    var texts = ArrayList<String>()
        private set
    var answers = ArrayList<Answer>()
        private set
    var className : String? = null
        private set

    public fun id(id: String) : PhraseTextBuilder
    {
        this.id = id
        return this
    }

    public fun answers(answers: ArrayList<Answer>) : PhraseTextBuilder
    {
        this.answers = answers
        return this
    }

    public fun className(className : String) : PhraseTextBuilder {
        this.className = className
        return this
    }

    public fun addAnswers(answer: Answer) : PhraseTextBuilder
    {
        this.answers.add(answer)
        return this
    }

    public fun texts(texts: ArrayList<String>) : PhraseTextBuilder
    {
        this.texts = texts
        return this
    }

    public fun addText(text: String) : PhraseTextBuilder
    {
        this.texts.add(text)
        return this
    }

    public fun build() : PhraseText {
        if(
            id == null
            || texts.isEmpty()
            || answers.isEmpty()
            || className == null
        ) {
            throw IllegalStateException("$this can not be created" )
        }

        return PhraseText(id!!, texts.toTypedArray(), answers.toTypedArray(), className!!)
    }

    override fun toString(): String {
        return "{id=$id, text=${texts.toTypedArray().contentToString()}, answers=${answers.toTypedArray().contentToString()}, className=$className}"
    }

}