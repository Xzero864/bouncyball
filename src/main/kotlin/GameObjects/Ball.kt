import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.Font
import kotlin.random.Random
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import org.json.JSONObject

/**
 * Ball.kt
 *
 * This program creates an interactive application featuring bouncing balls,
 * built using JavaFX.
 *
 * Author: Jason Silva (jason_silva@brown.edu
 * License: MIT License
 */

/********************************************************************************/
/*										*/
/*	Ball Class								*/
/*										*/
/********************************************************************************/

class Ball(private val width: Double, private val height: Double, private val radius: Double) {

    private var x = Random.nextDouble(width)
    private var y = Random.nextDouble(height)
    private var dx = Random.nextDouble(-1.0, 1.0)
    private var dy = Random.nextDouble(-1.0, 1.0)
    private var stockTicker = getUniqueTicker()
    private var percentageChange = fetchPercentageChange(stockTicker)
    private var color = if (percentageChange >= 0) Color.rgb(0, 200, 0) else Color.rgb(255, 69, 0)

    fun updatePosition() {
        x += dx
        y += dy
        if (x <= 0 || x >= width - radius) dx *= -1
        if (y <= 0 || y >= height - radius) dy *= -1
    }

    fun render(gc: GraphicsContext) {
        gc.fill = color
        gc.fillOval(x, y, radius * 2, radius * 2)
        val centerX = x + radius
        val centerY = y + radius
        gc.fill = Color.WHITE
        gc.font = Font.font("Arial", radius * 0.5)
        gc.fillText(stockTicker, centerX, centerY - radius * 0.2)
        gc.font = Font.font("Arial", radius * 0.3)
        gc.fillText(String.format("%.2f%%", percentageChange), centerX, centerY + radius * 0.3)
    }

    private fun fetchPercentageChange(ticker: String): Double {
        if (ticker.isEmpty()) return 0.0
        val apiKey = "GlGI4mhXMwpkrtzMO9HRJdFmtBflbQfVNjZ772W6"
        val url = "https://api.stockdata.org/v1/data/quote?symbols=$ticker&api_token=$apiKey"
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val response = connection.inputStream.bufferedReader(Charset.forName("UTF-8")).readText()
            val json = JSONObject(response)
            val changePercent = json.getJSONArray("data")
                .getJSONObject(0)
                .getDouble("percent_change")
            changePercent
        } catch (e: Exception) {
            Random.nextDouble(-10.0, 10.0)
        }
    }

    companion object {
        val availableTickers = mutableListOf(
            "AAPL", "MSFT", "AMZN", "GOOGL", "META", "TSLA", "BRK.B", "NVDA", "JPM", "JNJ",
            "V", "UNH", "HD", "PG", "MA", "BAC", "DIS", "NFLX", "KO", "PEP",
            "MRK", "PFE", "CSCO", "COST", "WMT", "CVX", "XOM", "INTC", "VZ", "T",
            "ABBV", "ACN", "ADBE", "AMD", "AMGN", "AXP", "BA", "BK", "BLK", "BMY",
            "C", "CAT", "CL", "CMCSA", "COP", "CRM", "DE", "DHR", "DOW", "DUK",
            "EMR", "FDX", "GD", "GE", "GS", "HON", "IBM", "ISRG", "LLY", "LMT",
            "LOW", "LRCX", "MDLZ", "MMM", "MO", "MS", "NEE", "NKE", "ORCL", "PEP",
            "PLD", "PYPL", "QCOM", "RTX", "SBUX", "SO", "SPGI", "TGT", "TXN", "UPS",
            "USB", "WBA", "WFC", "WM", "ZTS", "APA", "APD", "BAX", "BDX", "CARR",
            "CF", "CHD", "CI", "COF", "CTAS", "CVS", "D", "DG", "DLR", "DTE",
            "ECL", "EIX", "ETN", "F", "FIS", "FRC", "FTNT", "GLW", "HCA", "HIG",
            "HLT", "HSY", "ITW", "KHC", "KMI", "LHX", "LYB", "MCD", "MCO", "MKC",
            "MSI", "NOC", "NRG", "NUE", "O", "OKE", "OTIS", "PGR", "PKI", "PLTR",
            "PM", "PNC", "PPG", "PSA", "ROK", "RSG", "SIVB", "SNPS", "STT", "STZ",
            "SWK", "SYK", "TDG", "TER", "TJX", "TROW", "TSCO", "UHS", "VLO", "VRSK",
            "VTR", "WEC", "WY", "XYL", "ZBH", "ZION", "ADP", "AEP", "AIG", "AKAM",
            "ALL", "AMT", "ANET", "AON", "APTV", "ARE", "ATO", "AWK", "BEN", "BIIB",
            "BKR", "BXP", "CDW", "CE", "CHRW", "CLX", "CMS", "CNC", "CPB", "CSX",
            "CTSH", "CTVA", "DHI", "DLTR", "DOV", "DXCM", "ED", "EFX", "EL", "EPAM",
            "EVRG", "EXPD", "FMC", "FOX", "FTV", "GILD", "GIS", "HAL", "HBAN", "HSIC",
            "IP", "IRM", "K", "KEY", "KMB", "KR", "LEG", "LUV", "MGM", "MHK",
            "MKTX", "MSFT", "NEM", "NI", "NRZ", "OKE", "OMC", "ON", "ORLY", "OTIS",
            "OXY", "PARA", "PCAR", "PFE", "PG", "PXD", "RE", "RF", "RHI", "RJF",
            "RMD", "RNG", "SNA", "STE", "STLD", "SWKS", "TAP", "TDY", "TT", "UAA",
            "UAL", "UHS", "URI", "VAR", "VFC", "VMC", "VNO", "WAT", "WDC", "WHR"
        )

        fun getUniqueTicker(): String {
            return if (availableTickers.isNotEmpty()) {
                availableTickers.removeAt(Random.nextInt(availableTickers.size))
            } else {
                ""
            }
        }
    }
}
