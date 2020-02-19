import dialog.DialogCreator
import tools.ArgsInput

class Main {
    companion object{
        @JvmStatic
        fun main(args: Array<String>) {

            ArgsInput.readArgs(args)
            ArgsInput.checkConfig();
            DialogCreator.createDialogs(Configs.INPUT_FOLDER);
        }
    }
}
