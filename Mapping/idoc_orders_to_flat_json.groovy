import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*

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
    File outputFile = new File("$dataDir//idoc_orders_flat_json_output.txt")

    def inputBody = inputFile.getText("UTF-8")
    def outputBody = DoMapping(inputBody, headers, props)

    println outputBody
    outputFile.write outputBody
}

def DoMapping(String body, Map headers, Map properties) {
    def InputPayload = new XmlSlurper().parseText(body)

    def writer = new StringWriter()
    def builder = new StreamingJsonBuilder(writer)

    // MAPPING
    builder {
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

            row(this_IDOC.E1EDP01) { this_E1EDP01 ->
                idoc_no(v_idoc_no)
                order_no(v_order_no)
                ship_to(v_ship_to)
                sold_to(v_sold_to)
                order_date(v_order_date)

                line_item(this_E1EDP01.POSEX.toString())
                this_E1EDP01.E1EDP19.each { this_E1EDP19 ->
                    if (this_E1EDP19.QUALF.toString() == "001") {
                        material(this_E1EDP19.IDTNR.toString().replaceFirst("^0*", ""))
                    }
                }
                quantity(this_E1EDP01.MENGE.toString())
                quantity_uom(this_E1EDP01.MENEE.toString().toLowerCase())
                weight(this_E1EDP01.NTGEW.toString())
                weight_uom(this_E1EDP01.GEWEI.toString().toLowerCase())
            }
        }
    }

    def output = JsonOutput.prettyPrint(writer.toString())

    return output
}
