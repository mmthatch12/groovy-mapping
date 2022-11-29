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

    File inputFile = new File("$dataDir//json_product.txt")
    File outputFile = new File("$dataDir//json_product_appended.txt")

    def inputBody = inputFile.getText("UTF-8")
    def outputBody = DoMapping(inputBody, headers, props)

    println outputBody
    outputFile.write outputBody
}

def DoMapping(String body, Map headers, Map properties) {
    def payload_appended = properties.get("payload_appended")

    if(payload_appended == null){
        payload_appended = "[]"
    }

    def InputPayload = new JsonSlurper().parseText(body)
    def AppendedPayload = new JsonSlurper().parseText(payload_appended)

    AppendedPayload = AppendedPayload + InputPayload.value
    def output = JsonOutput.toJson(AppendedPayload)
    output = JsonOutput.prettyPrint(output)

    properties.put("payload_appended", output)


    def nextLink = InputPayload.'@odata.nextLink' ?: ""

    if(nextLink != "") {
        properties.put("has_more_records", "true")

        def queryString = nextLink.split('\\?')[1]
        queryString.split('&').each {
            if (it.startsWith('$top')) {
                properties.put("p_top", it.split('=')[1].toString())
            }
            if (it.startsWith('$skiptoken')) {
                properties.put("p_skip", it.split('=')[1].toString())
            }
        }
    }
    else{
        properties.put("has_more_records", "false")
    }

    return output
}
