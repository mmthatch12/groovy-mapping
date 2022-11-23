import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*
import org.supercsv.cellprocessor.*
import org.supercsv.cellprocessor.constraint.*
import org.supercsv.cellprocessor.ift.*
import org.supercsv.io.*
import org.supercsv.prefs.*
import org.supercsv.quote.*

def Message processData(Message message) {
    def body = message.getBody(String)
    def headers = message.getHeaders()
    def properties = message.getProperties()

    message.setBody(DoMapping(body, headers, properties))

    return message
}

//Need comment this TestRun() before upload to CPI. This TestRun() for local debug only
//TestRun()

void TestRun() {
    def scriptDir = new File(getClass().protectionDomain.codeSource.location.toURI().path).parent
    def dataDir = scriptDir + "//Data"

    Map headers = [:]
    Map props = [:]

    File inputFile = new File("$dataDir//json_tree.txt")
    File outputFile = new File("$dataDir//json_tree_csv_output.txt")

    def inputBody = inputFile.getText("UTF-8")
    def outputBody = DoMapping(inputBody, headers, props)

    println outputBody
    outputFile.write outputBody
}

def DoMapping(String body, Map headers, Map properties) {
    def InputPayload = new JsonSlurper().parseText(body)

    String[] header = ["order_no", "ship_to", "sold_to", "order_date", "line_item", "material", "quantity", "quantity_uom", "weight", "weight_uom"]
    def columnsCount = header.length
    CellProcessor[] processors = new CellProcessor[columnsCount]

    def sw = new StringWriter()

    //Use default standard preference
    def mapWriter = new CsvMapWriter(sw, CsvPreference.STANDARD_PREFERENCE)

    /*Use default standard preference with QuoteMode*/
//    CsvPreference StandardAlwaysQuote = new CsvPreference.Builder(CsvPreference.STANDARD_PREFERENCE).useQuoteMode(new AlwaysQuoteMode()).build()
//    def mapWriter = new CsvMapWriter(sw, StandardAlwaysQuote)

    /*Use custom preference - Pipe delimited*/
//    char quoteChar = '"'
//    int delimiterChar = '|'
//    String endOfLine = "\r\n"
//    CsvPreference PipeDelimited = new CsvPreference.Builder(quoteChar, delimiterChar, endOfLine).build()
//    def mapWriter = new CsvMapWriter(sw, PipeDelimited)

    /*If header not required no need use writeHeader*/
    mapWriter.writeHeader(header)

    InputPayload.orders.each { this_order ->
        def this_header = this_order.header

        this_order.item.each { this_item ->
            def row = [:]
            row.put("order_no", this_header.order_no ?: "")
            row.put("ship_to", this_header.ship_to ?: "")
            row.put("sold_to", this_header.sold_to ?: "")
            row.put("order_date", this_header.order_date ?: "")
            row.put("line_item", this_item.line_item ?: "")
            row.put("material", (this_item.material ?: "").padLeft(18, "0"))

            row.put("quantity", this_item.quantity ?: "")
            row.put("quantity_uom", (this_item.quantity_uom ?: "").toUpperCase())
            row.put("weight", this_item.weight ?: "")
            row.put("weight_uom", (this_item.weight_uom ?: "").toUpperCase())

            mapWriter.write(row, header, processors)
        }
    }

    mapWriter.close()

    def ouput = sw.toString()
    return ouput
}
