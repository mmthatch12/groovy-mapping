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

    File inputFile = new File("$dataDir//hq_invoice.txt")
    File outputFile = new File("$dataDir//invoice_output.txt")

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
                        DOCNUM(this_invoice.oinvoice_id ?: "")
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
//                      document header partner information can loop to get all parties (sold to (AG), ship to(WE)) CUSTOMER INFO
                    E1EDKA1(SEGMENT: '1') {
//                        Partner function (e.g. sold-to party, ship-to party, …) – field length: 3
                        PARVW("AG")
//                        PARTN – Partner number – field length: 17
                        PARTN(this_invoice.customer.ocustomer_id ?: "")
//                        Vendor number at customer location – field length: 17
                        LIFNR("")
//                        Name 1 – field length: 35
                        NAME1(this_invoice.customer.ocustomer_company ?: "")
//                        Street and house number 1 – field length: 35
                        STRAS(this_invoice.customer.billing_address.oaddress_street_1 ?: "")
//                        Street and house number 2 – field length: 35
                        STRS2(this_invoice.customer.billing_address.oaddress_street_2 ?: "")
//                        PO Box – field length: 35
                        PFACH("")
//                        City – field length: 35
                        ORT01(this_invoice.customer.billing_address.oaddress_locality ?: "")
//                        County code – field length: 9
                        COUNC(this_invoice.customer.billing_address.oaddress_region ?: "")
//                        Postal code – field length: 9
                        PSTLZ(this_invoice.customer.billing_address.oaddress_postcode ?: "")
//                        Country Key – field length: 3
                        LAND1(this_invoice.customer.billing_address.country.ocountry_code ?: "")
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
                        BELNR(this_invoice.oinvoice_id ?: "")
//                        Item number – field length: 6
                        POSNR("")
                        DATUM(this_invoice.oinvoice_date ?: "")
                    }

//                    document header date segment loop
                    E1EDK03(SEGMENT: '1') {
                        IDDAT("012")
                        DATUM(this_invoice.oinvoice_date ?: "")
                    }
//                      document header conditions
                    E1EDK05(SEGMENT: '1') {
//                        Surcharge or discount indicator – field length: 3
                        ALCKZ("")
//                        Condition type (coded) – field length: 4
                        KSCHL("")
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

//                    Document Header Terms of Payment opayment_term_id comes from order
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

//                    Document Header Bank Data
                    E1EDK17(SEGMENT: '1') {
//                        Country Key – field length: 3
                        BCOUN("")
//                        Bank name – field length: 70
                        BNAME("")
//                        Account number in bank data – field length: 30
                        ACNUM("")
//                        Account holder in bank data – field length: 35
                        ACNAM("")
                    }

//                    E1EDK29 – Document Header General Foreign Trade Data
//                    E1EDKT1 – Document Header Text Identification
//                    E1EDKT2 – Document Header Texts

//                    Document Header Organizational Data
                    E1EDK14(SEGMENT: '1') {
//                        IDOC qualifer organization – field length: 3
                        QUALF("")
//                        IDOC organization – field length: 35
                        ORGID("")
                    }

                    this_invoice.invoiceItems.each{ this_item ->
//                        Document Item General Data ITEMS DATA Loop through these
                        E1EDP01(SEGMENT: '1') {
//                        Item number – field length: 6
                            POSEX(this_invoice.oinvoice_item_id ?: "")
//                        Quantity – field length: 15
                            MENGE(this_invoice.oinvoice_item_quantity ?: "")
//                        Unit of measure – field length: 3
                            MENEE("")
//                        Net weight – field length: 18
                            NTGEW("")
//                        Weight unit – field length: 3
                            GEWEI("")
//                        Total weight – field length: 18
                            BRGEW("")
//                        Sales document item category – field length: 4
                            PSTYV("")
//                        Plant – field length: 4
                            WERKS("")

//                        Document Item Reference Data loopable inside outer E1EDP01
                            E1EDP02(SEGMENT: '1') {
//                            IDOC qualifier reference document – field length: 3
                                QUALF("")
//                            IDOC document number – field length: 35
                                BELNR("")
//                            IDOC: Date – field length: 8
                                DATUM("")
                            }

//                        Document Item Date Segment
                            E1EDP03(SEGMENT: '1') {
//                            Qualifier for IDOC date segment – field length: 3
                                IDDAT("")
//                            Date – field length: 8
                                DATUM("")
                            }

//                        Document Item Object Identification
                            E1EDP19(SEGMENT: '1') {
//                            IDOC object identification such as material no.,customer – field length: 3
                                QUALF("")
//                            IDOC material ID – field length: 35
                                IDTNR("")
                            }

//                          Document Item Amount Segment
                            E1EDP26(SEGMENT: '1') {
//                            Qualifier amount – field length: 3
                                QUALF("")
//                            Total value of sum segment – field length: 18
                                BETRG(this_invoice.oinvoice_item_amount ?: "")
                            }

//                        Document Item Partner Information
                            E1EDPA1(SEGMENT: '1') {
//                            Partner function (e.g. sold-to party, ship-to party, …) – field length: 3
                                PARVW("")
//                            Partner number – field length: 17
                                PARTN("")
//                            Name 1 – field length: 35
                                NAME1("")
//                            Street and house number 1 – field length: 35
                                STRAS("")
//                            City – field length: 35
                                ORT01("")
//                            Postal code – field length: 9
                                PSTLZ("")
//                            Country Key – field length: 3
                                LAND1("")
//                            1st telephone number of contact person – field length: 25
                                TELF1("")
//                            Fax number – field length: 25
                                TELFX("")
//                            Language key – field length: 1
                                SPRAS("")
//                            2-Character SAP Language Code – field length: 2
                                SPRAS_ISO("")
                            }

//                        E1EDP05 – Document Item Conditions
//                        E1EDP04 – Document Item Taxes
//                        E1EDP28 – Document Item – General Foreign Trade Data
//                        E1EDP08 – Package Data Individual
//                        E1EDP30 – Document Item Account Assignment Intercompany Billing
//                        E1EDPT1 – Document Item Text Identification
//                        E1EDPT2 – Document Item Texts
                        }
                    }

//                    Summary segment general
                    E1EDS01(SEGMENT: '1') {
//                        Qualifier for totals segment for shipping notification – field length: 3
                        SUMID("")
//                        Total value of sum segment – field length: 18
                        SUMME(this_invoice.oinvoice_total ?: "")
//                        Currency – field length: 3
                        WAERQ("USD")
                    }
                }
            }
        }
    }

    sw << builder.bind(idocMarkup)
    def output = XmlUtil.serialize(sw.toString())

    return output
}


