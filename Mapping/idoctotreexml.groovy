import com.sap.gateway.ip.core.customdev.util.Message
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil;
import groovy.xml.*

def Message processData(Message message) {
    def body = message.getBody(String);
    def headerContent = message.getHeaders();
    def properties = message.getProperties();

    message.setBody(DoMapping(body,headerContent,properties));

    return message
}

TestRun();

void TestRun() {
    def scriptDir = new File(getClass().protectionDomain.codeSource.location.path).parent;
    def dataDir = scriptDir + "//Data";

    Map headers = [:];
    Map props = [:];

    File inputFile = new File("$dataDir//idoc_orders.txt");
    File outputFile = new File("$dataDir//idoc_orders_tree_output.txt");

    def inputBody = inputFile.getText("UTF-8");
    def outputBody = DoMapping(inputBody, headers, props);

    println outputBody;
    outputFile.write outputBody;
}

def DoMapping(String body, Map headerContent, Map properties) {
    def InputPayload = new XmlSlurper().parseText(body)

    def writer = new StringWriter()
    def builder = new StreamingMarkupBuilder()

    // MAPPING
    def OUTPUT_MARKUP = {
        mkp.xmlDeclaration()

        orders {
            InputPayload.IDOC.each { this_IDOC ->
                order {
                    header {
                        idoc_no(this_IDOC.EDI_DC40.DOCNUM.toString().replaceFirst("^0*", ""))
                        order_no(this_IDOC.E1EDK01.BELNR.toString())

//                        this_IDOC.E1EDKA1.each { this_E1EDKA1 ->
//                            if (this_E1EDKA1.PARVW.toString() == "WE") {
//                                ship_to(this_E1EDKA1.PARTN.toString())
//                            }
//                            if (this_E1EDKA1.PARVW.toString() == "AG") {
//                                sold_to(this_E1EDKA1.PARTN.toString())
//                            }
//                        }
//
//                        this_IDOC.E1EDK03.each { this_E1EDK03 ->
//                            if (this_E1EDK03.IDDAT.toString() == "012") {
//                                order_date(this_E1EDK03.DATUM.toString())
//                            }
//                        }
//                    }
//                    this_IDOC.E1EDP01.each { this_E1EDP01 ->
//                        item {
//                            line_item(this_E1EDP01.POSEX.toString())
//
//                            this_E1EDP01.E1EDP19.each { this_E1EDP19 ->
//                                if (this_E1EDP19.QUALF.toString() == "001") {
//                                    material(this_E1EDP19.IDTNR.toString().replaceFirst("^0*", ""))
//                                }
//                            }
//
//                            quantity(this_E1EDP01.MENGE.toString())
//                            quantity_uom(this_E1EDP01.MENEE.toString().toLowerCase())
//                            weight(this_E1EDP01.NTGEW.toString())
//                            weight_uom(this_E1EDP01.GEWEI.toString().toLowerCase())
//
//                            schedule {
//                                this_E1EDP01.E1EDP20.each { this_E1EDP20 ->
//                                    line {
//                                        schedule_qty(this_E1EDP20.WMENG.toString())
//                                        schedule_date(this_E1EDP20.EDATU.toString())
//                                    }
//                                }
//                            }
//                        }
                    }
                }
            }
        }
    }

    // WRITING
    writer << builder.bind(OUTPUT_MARKUP)
    //def output = writer.toString()
    def output = XmlUtil.serialize(writer.toString())

    return output
}
