package me.paulrobinson.data.filedata

import java.io.File

class FileLoader {

    fun loadUserStocks() {
        val userStocks = HashSet<String>()
        File("test.txt").forEachLine {
            userStocks.add(it)
        }
    }

}