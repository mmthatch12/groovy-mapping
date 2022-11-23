import com.sap.gateway.ip.core.customdev.util.Message
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
TestRun()

void TestRun() {
    def scriptDir = new File(getClass().protectionDomain.codeSource.location.toURI().path).parent
    def dataDir = scriptDir + "//Data"

    Map headers = [:]
    Map props = [:]

    File inputFile = new File("$dataDir//idoc_orders.txt")
    File outputFile = new File("$dataDir//idoc_orders_csv_output.txt")

    def inputBody = inputFile.getText("UTF-8")
    def outputBody = DoMapping(inputBody, headers, props)

    println outputBody
    outputFile.write outputBody
}

def DoMapping(String body, Map headers, Map properties) {
    def InputPayload = new XmlSlurper().parseText(body)

    String[] header = ["idoc_no", "order_no", "ship_to", "sold_to", "order_date", "line_item", "material", "quantity", "quantity_uom", "weight", "weight_uom"]
    def columnsCount = header.length
    CellProcessor[] processors = new CellProcessor[columnsCount]

    def sw = new StringWriter()

    //Use default standard preference
//    def mapWriter = new CsvMapWriter(sw, CsvPreference.STANDARD_PREFERENCE)

    /*Use default standard preference with QuoteMode*/
//    CsvPreference StandardAlwaysQuote = new CsvPreference.Builder(CsvPreference.STANDARD_PREFERENCE).useQuoteMode(new AlwaysQuoteMode()).build()
//    def mapWriter = new CsvMapWriter(sw, StandardAlwaysQuote)

    /*Use custom preference - Pipe delimited*/
    char quoteChar = '"'
    int delimiterChar = '|'
    String endOfLine = "\r\n"
    CsvPreference PipeDelimited = new CsvPreference.Builder(quoteChar, delimiterChar, endOfLine).build()
    def mapWriter = new CsvMapWriter(sw, PipeDelimited)

    /*If header not required no need use writeHeader*/
    mapWriter.writeHeader(header)

    InputPayload.IDOC.each { this_IDOC ->
        String v_idoc_no = ''
        String v_order_no = ''
        String v_ship_to = ''
        String v_sold_to = ''
        String v_order_date = ''

        v_idoc_no = this_IDOC.EDI_DC40.DOCNUM.toString().replaceFirst("^0*", "")
        v_order_no = this_IDOC.E1EDK01.BELNR.toString()

        this_IDOC.E1EDKA1.each { this_E1EDKA1 ->
            if (this_E1EDKA1.PARVW.toString() == "WE") {
                v_ship_to = this_E1EDKA1.PARTN.toString()
            }
            if (this_E1EDKA1.PARVW.toString() == "AG") {
                v_sold_to = this_E1EDKA1.PARTN.toString()
            }
        }

        this_IDOC.E1EDK03.each { this_E1EDK03 ->
            if (this_E1EDK03.IDDAT.toString() == "012") {
                v_order_date = this_E1EDK03.DATUM.toString()
            }
        }

        this_IDOC.E1EDP01.each { this_E1EDP01 ->
            def row = [:]
            row.put("idoc_no", v_idoc_no)
            row.put("order_no", v_order_no)
            row.put("ship_to", v_ship_to)
            row.put("sold_to", v_sold_to)
            row.put("order_date", v_order_date)
            row.put("line_item", this_E1EDP01.POSEX.toString())

            this_E1EDP01.E1EDP19.each { this_E1EDP19 ->
                if (this_E1EDP19.QUALF.toString() == "001") {
                    row.put("material", this_E1EDP19.IDTNR.toString().replaceFirst("^0*", ""))
                }
            }
            row.put("quantity", this_E1EDP01.MENGE.toString())
            row.put("quantity_uom", this_E1EDP01.MENEE.toString().toLowerCase())
            row.put("weight", this_E1EDP01.NTGEW.toString())
            row.put("weight_uom", this_E1EDP01.GEWEI.toString().toLowerCase())

            mapWriter.write(row, header, processors)
        }
    }

    mapWriter.close()

    def ouput = sw.toString()
    return ouput
}
