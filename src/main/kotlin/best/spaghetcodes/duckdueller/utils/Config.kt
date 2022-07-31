package best.spaghetcodes.duckdueller.utils

import best.spaghetcodes.duckdueller.DuckDueller
import java.io.File

const val fileName = "duckdueller.cfg"

object Config {

    private val config = HashMap<String, Any>()

    // set the config
    // needed if there is no config file yet or if new values get added later on
    init {
        config["minCPS"] = 10
        config["maxCPS"] = 14
        config["lookSpeed"] = 10f
        config["webhook"] = ""
        config["ggDelay"] = 100
        config["ggMessage"] = "gg"
        config["rqDelay"] = 1000
        config["maxDistanceLook"] = 150
        config["maxDistanceAttack"] = 15
        config["apiKey"] = ""
        config["dodgeWins"] = 4000
        config["dodgeWS"] = 20
        config["dodgeWLR"] = 4
        config["dodgeLost"] = true
        config["dodgeNoStats"] = true
    }

    fun load() {
        println("Loading config")

        val file = File(DuckDueller.mc.mcDataDir.absolutePath + "/config/" + fileName)

        if (file.exists()) {
            val lines = file.readLines()
            for (line in lines) { // parse the file line by line
                if (line.contains("=")) {
                    val split = line.split("=")
                    if (split.size == 2) {
                        when (config[split[0]]) { // make sure to use the correct type
                            is Int -> {
                                config[split[0]] = split[1].toInt()
                            }
                            is String -> {
                                config[split[0]] = split[1]
                            }
                            is Boolean -> {
                                config[split[0]] = split[1].toBoolean()
                            }
                        }
                    }
                }
            }
            println("Loaded config")
        } else {
            save();
        }
    }

    fun save() {
        val file = File(DuckDueller.mc.mcDataDir.absolutePath + "/config/" + fileName)

        if (!file.exists()) {
            file.createNewFile()
        }

        val writer = file.writer()
        for (entry in config) {
            writer.write(entry.key + "=" + entry.value + "\n")
        }
        writer.close()
    }

    fun set(key: String, value: Any) {
        config[key] = value
    }

    fun get(key: String): Any? {
        if (config.containsKey(key)) {
            return config[key]
        }
        return null
    }

}