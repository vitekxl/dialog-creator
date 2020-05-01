package dialog.creator.tools

import models.items.text.PhraseText
import java.lang.StringBuilder

class StringTool {
    companion object{
        public fun fixText(srt: String): String{
                var text= srt;
                text = trim(text)
                text = fixPunctuationMark(text)
                text = fixDoubleSpaces(text)
                return text;

        }

        private fun trim(str: String): String{
            return StringBuilder(str.trim()).toString()
        }

        private fun fixPunctuationMark(str: String): String{
            if(str.length < 3) return str;
            val sb = StringBuffer(str);
            val marks = arrayOf('.', '!', '?', ',')
            for (i in 1 until sb.length-1 step 2) {
               if(
                   marks.contains(sb[i])
                   && !marks.contains(sb[i-1])
                   && !marks.contains(sb[i+1])
                   && sb[i+1] != ' '
               ) sb.insert(i+1, ' ');
            }
            return sb.toString();
        }

        private fun fixDoubleSpaces(str: String): String {
                return str.replace("  ", " ");
        }

    }


}