/**
 * Main.kt
 *
 * This program creates an interactive application featuring bouncing balls,
 * built using JavaFX.
 *
 * Author: Jason Silva (jason_silva@brown.edu
 * License: MIT License
 */

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.random.Random
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope




// ======================================================
// Constants and Variables
// ======================================================

private const val API_KEY = "GlGI4mhXMwpkrtzMO9HRJdFmtBflbQfVNjZ772W6" // Replace with your StockData.org API key
private const val NUM_STOCKS = 100
private const val NUM_CHUNKS = 5



// ======================================================
// StockDataBouncingBallsApp Class
// ======================================================

/**
 * StockDataBouncingBallsApp
 *
 * The main application class creates the bouncing balls app, makes api calls, and uses seperate couroutines
 */
class StockDataBouncingBallsApp : Application() {

    // Mutable list to hold Ball objects
    private val balls = mutableListOf<Ball>()

    override fun start(primaryStage: Stage) {
        val canvas = Canvas(800.0, 600.0)
        val gc = canvas.graphicsContext2D


        val backgroundColor = Color.rgb(30, 30, 30)

        val root = StackPane(canvas)
        root.style = "-fx-background-color: #1e1e1e;"

        CoroutineScope(Dispatchers.IO).launch {
            val (stockPrices, minMax) = fetchStockPrices()
            val (minPrice, maxPrice) = minMax

            stockPrices.forEach { price ->
                balls.add(Ball(canvas.width, canvas.height, scalePriceToRadius(price, minPrice, maxPrice)))
            }

            val chunkSize = balls.size / NUM_CHUNKS
            val ballChunks = balls.chunked(chunkSize)

            ballChunks.forEach { ballChunk ->
                launch {
                    while (true) {
                        ballChunk.forEach { it.updatePosition() }
                        delay(20)
                    }
                }
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                withContext(Dispatchers.Main) {

                    gc.fill = backgroundColor
                    gc.fillRect(0.0, 0.0, canvas.width, canvas.height)

                    balls.forEach { it.render(gc) }
                }
                delay(20)
            }
        }

        primaryStage.title = "Bouncing Balls Stock Data"
        primaryStage.scene = Scene(root, 800.0, 600.0)
        primaryStage.show()
    }


    /**
     * Scales a given stock price to radius, based on min/max stock values
     *
     * @param price the price to scale
     * @param minPrice The minimum stock price
     * @param maxPrice See above
     * @return The scaled radius
     */
    private fun scalePriceToRadius(price: Double, minPrice: Double, maxPrice: Double): Double {
        val minRadius = 5.0
        val maxRadius = 30.0

        if (minPrice == maxPrice) return minRadius

        return ((price - minPrice) / (maxPrice - minPrice)) * (maxRadius - minRadius) + minRadius
    }



    /**
     * Fetches stock prices from StockData.org API concurrently and returns the minimum and maximum prices for scaling.
     *
     * @return A Pair containing a list of stock prices and the min and max values
     */
    private suspend fun fetchStockPrices(): Pair<List<Double>, Pair<Double, Double>> = coroutineScope {
        val client = OkHttpClient()
        val prices = mutableListOf<Double>()


        val priceDeferred = Ball.availableTickers.map { symbol ->
            async {
                val url = "https://api.stockdata.org/v1/data/quote?symbols=$symbol&api_token=$API_KEY"
                val request = Request.Builder().url(url).build()


                try {
                    val response = client.newCall(request).execute()
                    val json = JSONObject(response.body?.string() ?: "{}")
                    val dataArray = json.getJSONArray("data")
                    dataArray.getJSONObject(0).getDouble("price")
                } catch (e: Exception) {
                    println("Failed to fetch data for $symbol: ${e.message}")
                    null
                }
            }
        }


        val fetchedPrices = priceDeferred.mapNotNull { it.await() }
        prices.addAll(fetchedPrices)


        while (prices.size < NUM_STOCKS) {
            prices.add(Random.nextDouble(1.0, 300.0))
        }


        val minPrice = prices.minOrNull() ?: 1.0
        val maxPrice = prices.maxOrNull() ?: 300.0

        return@coroutineScope Pair(prices, Pair(minPrice, maxPrice))
    }



}

// ======================================================
// Main Function
// ======================================================

/**
 * The main entry point for the application.
 * @param args
 *
 */
fun main(args: Array<String>) {
    Application.launch(StockDataBouncingBallsApp::class.java)
}
