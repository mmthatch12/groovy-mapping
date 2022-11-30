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
TestRun()

void TestRun() {
    def scriptDir = new File(getClass().protectionDomain.codeSource.location.toURI().path).parent
    def dataDir = scriptDir + "//Data"

    Map headers = [:]
    Map props = [:]

    props.put("ReceiverPort", "SAPSID")
    props.put("ReceiverPartner", "SIDCLNT100")

    File inputFile = new File("$dataDir//hq_order.txt")
    File outputFile = new File("$dataDir//mapping_output.txt")

    def inputBody = inputFile.getText("UTF-8")
    def outputBody = DoMapping(inputBody, headers, props)

    println outputBody
    outputFile.write outputBody
}

def DoMapping(String body, Map headers, Map properties) {
    def InputPayload = new JsonSlurper().parseText(body)
    println InputPayload
    def sw = new StringWriter()
    def builder = new StreamingMarkupBuilder()

    //Required Idoc Control Record variables
    def V_TABNAM = "EDI_DC 40"
    def V_DIRECT = "2"
    def V_IDOCTYP = "ORDERS05"
    def V_CIMTYP = ""
    def V_MESTYP = "ORDERS"
    def V_SNDPOR = "XYZPORT" //Any port name represent source system
    def V_SNDPRT = "LS"
    def V_SNDPFC = ""
    def V_SNDPRN = "XYZ" //Any source system name
    def V_RCVPOR = properties.get("ReceiverPort").toString()
    def V_RCVPRT = "LS"
    def V_RCVPFC = ""
    def V_RCVPRN = properties.get("ReceiverPartner").toString()

    //Loop Pando Json record and create Idoc Xml segments, fields and values
    def idocMarkup = {
        mkp.xmlDeclaration()

        ORDERS05 {
            InputPayload.each { this_order ->
                IDOC(BEGIN: '1') {
                    EDI_DC40(BEGIN: '1') {
                        TABNAM("$V_TABNAM")
                        DIRECT("$V_DIRECT")
                        IDOCTYP("$V_IDOCTYP")
                        CIMTYP("$V_CIMTYP")
                        MESTYP("$V_MESTYP")
                        SNDPOR("$V_SNDPOR")
                        SNDPRT("$V_SNDPRT")
                        SNDPFC("$V_SNDPFC")
                        SNDPRN("$V_SNDPRN")
                        RCVPOR("$V_RCVPOR")
                        RCVPRT("$V_RCVPRT")
                        RCVPFC("$V_RCVPFC")
                        RCVPRN("$V_RCVPRN")
                    }

                    def this_header = this_order.header

                    E1EDK01(SEGMENT: '1') {
                        BELNR(this_order.oorder_id ?: "")
                    }
                    E1EDK03(SEGMENT: '1') {
                        IDDAT("012")
                        DATUM(this_order.oorder_date ?: "")
                    }
                    E1EDKA1(SEGMENT: '1') {
                        PARVW("AG")
                        DATUM(this_order.ocustomer_account_id ?: "")
                    }
                    E1EDKA1(SEGMENT: '1') {
                        PARVW("WE")
                        DATUM(this_order.ocustomer_account_id ?: "")
                    }

                    this_order.items.each{ this_item ->
                        E1EDP01(SEGMENT: '1') {
                            POSEX(this_item.oproduct_id ?: "")
                            MENGE(this_item.oorder_item_quantity ?: "")

//                            quantity unit of measurement
                            String V_MENEE = ""
                            V_MENEE = V_MENEE.toUpperCase()
                            MENEE(V_MENEE)

//                            weight
                            NTGEW("")

//                            weight unit of measurement
                            String V_GEWEI = ""
                            V_GEWEI = V_GEWEI.toUpperCase()
                            GEWEI(V_GEWEI)

//                            this_item.schedule.each{ this_schedule ->
//                                E1EDP20(SEGMENT: '1') {
//                                    WMENG(this_schedule.schedule_qty ?: "")
//                                    EDATU(this_schedule.schedule_date ?: "")
//                                }
//                            }
//
//                            E1EDP19(SEGMENT: '1') {
//                                QUALF("001")
//
//                                String V_IDTNR = this_item.material ?: ""
//                                V_IDTNR = V_IDTNR.padLeft(18, "0")
//                                IDTNR(V_IDTNR)
//                            }
                        }
                    }
                }
            }
        }
    }

    sw << builder.bind(idocMarkup)
    def output = XmlUtil.serialize(sw.toString())

    return output
}

