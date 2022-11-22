import com.sap.gateway.ip.core.customdev.util.Message;

//def Message processData(Message message) {
//    def body = message.getBody(String);
//    def headerContent = message.getHeaders();
//    def properties = message.getProperties();
//
//    message.setBody(DoMapping(body,headerContent,properties));
//
//    return message
//}

TestRun();

void TestRun() {
    def scriptDir = new File(getClass().protectionDomain.codeSource.location.path).parent;
    println scriptDir;
    def dataDir = scriptDir + "//Data";
    println dataDir;
    Map headers = [:];
    Map props = [:];

    headers.put("field1", "John");
    props.put("text1", "How are you?");

    File inputFile = new File("$dataDir//example.txt");
    File outputFile = new File("$dataDir//example_output.txt");

    def inputBody = inputFile.getText("UTF-8");
    println inputBody;
    def outputBody = DoMapping(inputBody, headers, props);

    println "field1=" + headers.get("field1") as String;
    println "text1=" + props.get("text1") as String;
    println "field2=" + headers.get("field2") as String;
    println "text2=" + props.get("text2") as String;

//    println outputBody;
//    outputFile.write outputBody;
}

def DoMapping(String body, Map headerContent, Map properties) {
    String output = "";

    String field1 = headerContent.get("field1") as String;
    String text1 = properties.get("text1") as String;
    headerContent.put("field1", field1 + " yep field1");
    headerContent.put("field2", "yep field2 yali");
    properties.put("text1",text1 + "yep text1");
    properties.put("text2", "yep text2 yali");

    output = body + " completely modified"

    return output
}
