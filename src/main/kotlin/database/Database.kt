package me.paulrobinson.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoException
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import me.paulrobinson.database.client.ClientData
import org.bson.BsonInt64
import org.bson.Document
import org.bson.UuidRepresentation

class Database {

    private val DATABASE_NAME = "stock_server"

    private val mongoClient : MongoClient = MongoClients.create(MongoClientSettings.builder()
        .uuidRepresentation(UuidRepresentation.STANDARD)
        .applyConnectionString(ConnectionString("mongodb://localhost:27017"))
        .build())

    companion object {
        val instance = Database()
    }

    init {
        try {
            // Send a ping to confirm a successful connection
            val command = Document("ping", BsonInt64(1))
            getClientDocument().run { find(command) }
            println("Pinged your deployment. You successfully connected to MongoDB!")
        } catch (me: MongoException) {
            System.err.println(me)
            null
        }


        if (getClientDocument() == null) {
            mongoClient.getDatabase(DATABASE_NAME).createCollection("client_data")
        }
        if (getStockDocument() == null) {
            mongoClient.getDatabase(DATABASE_NAME).createCollection("stock_data")
        }
    }

    fun testConnect() {
        val database = mongoClient.getDatabase(DATABASE_NAME)
        println(database.listCollectionNames())
        if (database != null) {
            println("Connected to database: ${database.name}")
        } else {
            println("Failed to connect to database")
        }
    }

    fun getClientDocument() : MongoCollection<Document>{
        return mongoClient.getDatabase(DATABASE_NAME).getCollection("client_data")
    }

    fun getStockDocument() : MongoCollection<Document>{
        return mongoClient.getDatabase(DATABASE_NAME).getCollection("stock_data")
    }

    fun loadClientData(username : String) : ClientData? {
        return ClientData("test", "test", HashSet())
        val clientData = getClientDocument().find(Filters.eq("username", username))
        if (clientData.count() == 0) {
            return null
        } else {
            val document = clientData.first()
            val password = document!!["password"] as String
            val stockPreferences = document["stock_preferences"] as HashSet<String>
            return ClientData(username, password, HashSet(stockPreferences))
        }

    }


}