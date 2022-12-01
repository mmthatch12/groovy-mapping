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
//                      document header partner information can loop to get all parties (sold to (AG), ship to(WE))
                    E1EDKA1(SEGMENT: '1') {
//                        Partner function (e.g. sold-to party, ship-to party, …) – field length: 3
                        PARVW("012")
//                        PARTN – Partner number – field length: 17
                        PARTN("")
//                        Vendor number at customer location – field length: 17
                        LIFNR("")
//                        Name 1 – field length: 35
                        NAME1("")
//                        Street and house number 1 – field length: 35
                        STRAS("")
//                        Street and house number 2 – field length: 35
                        STRS2("")
//                        PO Box – field length: 35
                        PFACH("")
//                        City – field length: 35
                        ORT01("")
//                        County code – field length: 9
                        COUNC("")
//                        Postal code – field length: 9
                        PSTLZ("")
//                        Country Key – field length: 3
                        LAND1("")
//                        Language key – field length: 1
                        SPRAS("")
//                        IDoc user name – field length: 35
                        BNAME("")
//                        IDOC organization code – field length: 30
                        PAORG("")
                    }

//                      document header reference data loop
                    E1EDK02(SEGMENT: '1') {
//                        IDOC qualifier reference document – field length: 3
                        QUALF("")
//                        IDOC document number – field length: 35
                        BELNR("")
//                        Item number – field length: 6
                        POSNR("")
                        DATUM(this_invoice.oorder_date ?: "")
                    }

//                    document header date segment loop
                    E1EDK03(SEGMENT: '1') {
                        IDDAT("012")
                        DATUM(this_invoice.oorder_date ?: "")
                    }
//                      document header conditions
                    E1EDK05(SEGMENT: '1') {
//                        Surcharge or discount indicator – field length: 3
                        ALCKZ("")
//                        Condition type (coded) – field length: 4
                        KSCHL(this_invoice.oorder_date ?: "")
//                        Condition text – field length: 80
                        KOTXT("")
//                        Fixed surcharge/discount on total gross – field length: 18
                        BETRG("")
//                        Condition percentage rate – field length: 8
                        KPERC("")
//                        Condition record per unit – field length: 15
                        KRATE("")
//                        Price unit – field length: 9
                        UPRBS("")
//                        Unit of measurement – field length: 3
                        MEAUN("")
//                        IDoc condition end amount – field length: 18
                        KOBTR("")
//                        VAT indicator – field length: 7
                        MWSKZ("")
//                        VAT rate – field length: 17
                        MSATZ("")
//                        Currency – field length: 3
                        KOEIN("")
                    }

//                    Document header taxes
                    E1EDK04(SEGMENT: '1') {
//                        VAT indicator – field length: 7
                        MWSKZ("")
//                        VAT rate – field length: 17
                        MSATZ("")
//                        Value added tax amount – field length: 18
                        MWSBT("")
                    }

//                    Document Header Terms of Delivery
                    E1EDK17(SEGMENT: '1') {
//                        IDOC qualifier: Terms of delivery – field length: 3
                        QUALF("")
//                        IDOC delivery condition code – field length: 3
                        LKOND("")
//                        IDOC delivery condition text – field length: 70
                        LKTEXT("")
                    }

//                    Document Header Terms of Payment
                    E1EDK18(SEGMENT: '1') {
//                        IDOC qualifier: Terms of payment – field length: 3
                        QUALF("")
//                        IDOC Number of days – field length: 8
                        TAGE("")
//                        IDOC percentage for terms of payment – field length: 8
                        PRZNT("")
//                        Text line – field length: 70
                        ZTERM_TXT("")
                    }

//                    Document Header Currency Segment
                    E1EDK23(SEGMENT: '1') {
//                        Qualifier currency – field length: 3
                        QUALF("")
//                        IDOC Number of days – field length: 8
                        WAERZ("")
//                        Three-digit character field for IDocs – field length: 3
                        WAERQ("")
//                        Character Field of Length 12 – field length: 12
                        KURS("")
//                        IDOC: Date – field length: 8
                        DATUM("")
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


