package me.paulrobinson

import me.paulrobinson.data.DataHandler




fun main() {
    println("Server Initializing...")


    //Then load data
    DataHandler().initialize()

    //Initialize client connection & interaction here


    //Maintain systems

    println("Server Initialized")

}