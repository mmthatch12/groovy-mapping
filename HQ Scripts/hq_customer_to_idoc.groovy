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

    File inputFile = new File("$dataDir//hq_customer.txt")
    File outputFile = new File("$dataDir//customer_output.txt")

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
    def V_IDOCTYP = "DEBMDM05"
//    not sure what this needs to be
    def V_MESTYP = "CUSTOMER"
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
            InputPayload.each { this_customer ->
                IDOC(BEGIN: '1') {
                    EDI_DC40(BEGIN: '1') {
                        TABNAM("$V_TABNAM")
                        MANDT("$V_MANDT")
                        DOCNUM(this_customer.ocustomer_id ?: "")
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

//                    Master customer master basic data
                    E1KNA1M(SEGMENT: '1') {
//                        Customer number 1 – field length: 10
                        KUNNR(this_customer.ocustomer_id ?: "")
//                        Title – field length: 15
                        ANRED("")
//                        Authorization Group – field length: 4
                        BEGRU("")
//                        Industry key – field length: 4
                        BRSCH("")
//                        Account number of the master record with the fiscal address – field length: 10
                        FISKN("")
//                        Group key – field length: 10
                        KONZS("")
//                        Customer Account Group – field length: 4
                        KTOKD("")
//                        Customer classification – field length: 2
                        KUKLA("")
//                        Country Key – field length: 3
                        LAND1("")
//                        Name 1 – field length: 35
                        NAME1(this_customer.ocustomer_company ?: "")
//                        City – field length: 35
                        ORT01("")
//                        PO Box – field length: 10
                        PFACH("")
//                        P.O. Box Postal Code – field length: 10
                        PSTL2("")
//                        Postal Code – field length: 10
                        PSTLZ("")
//                        Region (State, Province, County) – field length: 3
                        REGIO("")
//                        County Code – field length: 3
                        COUNC("")
//                        City Code – field length: 4
                        CITYC("")
//                        Tax Number 1 – field length: 16
                        STCD1("")
//                        Liable for VAT – field length: 1
                        STKZU("")
//                        Street and House Number – field length: 35
                        STRAS("")
//                        First telephone number – field length: 16
                        TELF1("")
//                        Second telephone number – field length: 16
                        TELF2("")
//                        Fax Number – field length: 31
                        TELFX("")
//                        Cash Discount – field length: 3
                        KATR10("")
                    }
//                      Customer Master: Additional General Fields (not sure if this should go inside above section
                    E1KNA11(SEGMENT: '1') {
//                        Name of Representative – field length: 10
                        J_1KFREPRE("")
//                        Type of Business – field length: 30
                        J_1KFTBUS("")
//                        Type of Industry – field length: 30
                        J_1KFTIND("")
//                        Name 1 – field length: 35
                        PSON1("")
                    }

//                      Master customer master sales data (KNVV)
                    E1KNVVM(SEGMENT: '1') {
//                        Sales Organization – field length: 4
                        VKORG("")
//                        Customer Group – field length: 2
                        KDGRP("")
//                        Customer Price Group – field length: 2
                        KONDA("")
//                        Incoterms (Part 1) – field length: 3
                        INCO1("")
//                        Currency – field length: 5
                        WAERS("")
//                        Account Assignment Group for this customer – field length: 2
                        KTGRD("")
//                        Terms of payment key – field length: 4
                        ZTERM("")
                    }

//                    Master customer master bank details and bank master
                    E1KNBKM(SEGMENT: '1') {
//                        Bank number – field length: 15
                        BANKL("")
//                        Bank account number – field length: 18
                        BANKN("")
//                        Name of bank – field length: 60
                        BANKA("")
//                        Street and House Number – field length: 35
                        STRAS("")
//                        City – field length: 35
                        ORT01("")
                    }
//                      Master customer master contact person (KNVK)
                    E1KNVKM(SEGMENT: '1') {
//                        Number of contact person – field length: 10
                        PARNR("")
//                        Name 1 – field length: 35
                        NAME1("")
//                        First telephone number – field length: 16
                        TELF1("")
                    }
                }
            }
        }
    }

    sw << builder.bind(idocMarkup)
    def output = XmlUtil.serialize(sw.toString())

    return output
}


