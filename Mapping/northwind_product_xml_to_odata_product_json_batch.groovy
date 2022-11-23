import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*
import groovy.xml.*

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

    File inputFile = new File("$dataDir//northwind_product_xml.txt")
    File outputFile = new File("$dataDir//northwind_product_xml_odata_batch_json_output.txt")

    def inputBody = inputFile.getText("UTF-8")
    def outputBody = DoMapping(inputBody, headers, props)

    println outputBody
    outputFile.write outputBody
}

def DoMapping(String body, Map headers, Map properties) {
    def InputPayload = new XmlSlurper().parseText(body)

    def sb = new StringBuilder()

    sb.with {
        append "--batch_xyz"
        append '\r\n' append "Content-Type: multipart/mixed; boundary=changeset_1"
        append '\r\n'

        InputPayload.row.each { this_row ->

            String V_ID = this_row.ID.toString()
            String V_Action = this_row.Action.toString()

            append '\r\n' append "--changeset_1"
            append '\r\n' append "Content-Type: application/http"
            append '\r\n' append "Content-Transfer-Encoding:binary"
            append '\r\n'
            if(V_Action == "C") {
                append '\r\n' append "POST Products HTTP/1.1"
            }
            else{
                String V_ID_URLEncoded = URLEncoder.encode(V_ID, "UTF-8")
                append '\r\n' append "PUT Products($V_ID_URLEncoded) HTTP/1.1"
            }
            append '\r\n' append "Content-Type: application/json"
            append '\r\n'

            def writer = new StringWriter()
            def builder = new StreamingJsonBuilder(writer)

            append '\r\n'
            builder {
                ID(this_row.ID)
                Name(this_row.Name.toString())
                Price(this_row.Price.toString())
            }
            append writer.toString()
            append '\r\n'

        }
        append '\r\n' append "--changeset_1--"
        append '\r\n'
        append '\r\n' append "--batch_xyz--"
    }

    def output = sb.toString()

    return output
}
