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
    def sw = new StringWriter()
    def builder = new StreamingMarkupBuilder()

    //Required Idoc Control Record variables
    def V_TABNAM = "EDI_DC40"
    def V_MANDT = "100"
    def V_DOCNUM = ""
    def V_DOCREL = ""
    def V_STATUS = ""
    def V_DIRECT = "1"
    def V_OUTMOD = ""
    def V_IDOCTYP = "INVOIC02"
    def V_MESTYP = "INVOIC"
    def V_CIMTYP = ""
    def V_SNDPOR = "XYZPORT" //Any port name represent source system
    def V_SNDPRT = "LS"
    def V_SNDPFC = ""
    def V_SNDPRN = "XYZ" //Any source system name
    def V_RCVPOR = "SAPSID"
    def V_RCVPRT = "LS"
    def V_RCVPFC = "LS"
    def V_RCVPRN = "SIDCLNT100"

    //Loop Pando Json record and create Idoc Xml segments, fields and values
    def idocMarkup = {
        mkp.xmlDeclaration()

        INVOIC02 {
            InputPayload.each { this_invoice ->
                IDOC(BEGIN: '1') {
                    EDI_DC40(BEGIN: '1') {
                        TABNAM("$V_TABNAM")
                        MANDT("$V_MANDT")
                        DOCNUM("$V_DOCNUM")
                        DOCREL("$V_DOCREL")
                        STATUS("$V_STATUS")
                        DIRECT("$V_DIRECT")
                        OUTMOD("$V_OUTMOD")
                        IDOCTYP("$V_IDOCTYP")
                        MESTYP("$V_MESTYP")
                        CIMTYP("$V_CIMTYP")
                        SNDPOR("$V_SNDPOR")
                        SNDPRT("$V_SNDPRT")
                        SNDPFC("$V_SNDPFC")
                        SNDPRN("$V_SNDPRN")
                        RCVPOR("$V_RCVPOR")
                        RCVPRT("$V_RCVPRT")
                        RCVPFC("$V_RCVPFC")
                        RCVPRN("$V_RCVPRN")
                    }

                    E1EDK01(SEGMENT: '1') {
                        CURCY("USD")
                        HWAER("")
//                        exchange rate
                        WKURS("")
//                        terms of payment key
                        ZTERM("")
//                        VAT Registration Number
                        KUNDEUINR("")
//                        VAT Registration Number
                        EIGENUINR("")
//                        Document Type
                        BSART("INVO")
//                        IDOC document Number
                        BELNR(this_invoice.oinvoice_id ?: "")
//                        netweight
                        NTGEW("")
//                        some kind of weight
                        BRGEW("")
//                        weight unit
                        GEWEI("")
//                        invoice list type
                        FKART_RL("")
//                        Number of Recipient
                        RECIPNT_NO("")
//                        billing category
                        FKTYP("")
                    }

                    E1EDK03(SEGMENT: '1') {
                        IDDAT("012")
                        DATUM(this_invoice.oorder_date ?: "")
                    }
                    E1EDKA1(SEGMENT: '1') {
                        PARVW("AG")
                        DATUM(this_invoice.ocustomer_account_id ?: "")
                    }
                    E1EDKA1(SEGMENT: '1') {
                        PARVW("WE")
                        DATUM(this_invoice.ocustomer_account_id ?: "")
                    }

                    this_invoice.items.each{ this_item ->
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


