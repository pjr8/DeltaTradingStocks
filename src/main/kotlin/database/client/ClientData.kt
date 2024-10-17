package me.paulrobinson.database.client

data class ClientData(val userName : String, val password : String, val stockPreferences : HashSet<String>)