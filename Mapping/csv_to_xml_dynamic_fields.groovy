import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.*
import groovy.xml.*
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

//    File inputFile = new File("$dataDir//csv_data_with_header.txt")
//    File outputFile = new File("$dataDir//csv_data_with_header_xml_output.txt")
    File inputFile = new File("$dataDir//csv_complex.txt")
    File outputFile = new File("$dataDir//csv_complex_xml_output.txt")

    def inputBody = inputFile.getText("UTF-8")
    def outputBody = DoMapping(inputBody, headers, props)

    println outputBody
    outputFile.write outputBody
}

def DoMapping(String body, Map headers, Map properties) {
    ICsvMapReader mapReader = new CsvMapReader(new StringReader(body), CsvPreference.STANDARD_PREFERENCE)
    String[] header = mapReader.getHeader(true)
    int columnsCount = header.length
    CellProcessor[] processor = new CellProcessor[columnsCount]
    def rowMap = [:]

    def writer = new StringWriter()
    def builder = new StreamingMarkupBuilder()

    def OutputMarkup = {
        root {
            while ((rowMap = mapReader.read(header, processor)) != null) {
                row {
                    for (int i = 0; i < header.length; i++) {
                        def fieldName = formatFieldName(header[i])
                        "$fieldName"(rowMap[header[i]])
                    }
                }
            }
        }
    }

    writer << builder.bind(OutputMarkup)
    def output = XmlUtil.serialize(writer.toString())

    return output
}

def formatFieldName(String fieldName) {
    fieldName = fieldName.replaceAll("[^a-zA-Z0-9]", "")
    fieldName = fieldName.toLowerCase()
    return fieldName
}