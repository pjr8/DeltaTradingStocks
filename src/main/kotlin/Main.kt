package me.paulrobinson

import me.paulrobinson.data.DataHandler
import me.paulrobinson.database.Database
import me.paulrobinson.server.Server
import java.awt.Desktop


fun main() {
    println("Server Initializing...")


    //val desktop = Desktop.getDesktop()
    //desktop.browse(java.net.URI("https://www.globenewswire.com/news-release/2024/10/11/2961843/28124/en/Healthcare-Mobility-Solutions-Industry-Report-2024-Market-Review-and-Forecast-2014-2030-with-Strategic-Analysis-of-42-Key-Players-Apple-McKesson-Microsoft-Oracle-Zebra-Technologies.html"))

    val database = Database()

    Server().initialize()

    //Then load data
    //DataHandler().initialize()

    //Initialize client connection & interaction here


    //Maintain systems

    println("Server Initialized")

}