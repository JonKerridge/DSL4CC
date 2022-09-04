package DSLparse

import DSLrecords.ParseRecord

class DSLParser {

  String inputFileName, outputTextFile, outObjectFile

  DSLParser(String inFileName) {
    this.inputFileName = inFileName
    outputTextFile = inputFileName + "txt"
    outObjectFile = inputFileName + "struct"
  }

  String hostAddress

   Integer findOption(List tokens, int tokenSize, String testFor) {
    Integer t
    t = 0
    while ((t < tokenSize && (!(tokens[t] =~ testFor)))) t++
    // need to ensure that at least one token follows the option specification
    if ((t+1) < tokenSize) return t else return null
  }

  boolean parse() {
    List<ParseRecord> buildData
    buildData = []
    boolean ok = true
    int line = 0
    boolean collectProcessed = false
    new File(inputFileName).eachLine { String inLine ->
      List<String> tokens = inLine.tokenize()
      switch (tokens[0]){
        case 'host':
          if (line == 0) ok = parseHost(tokens, buildData) else {
            println " Host specification not first line in $inputFileName"
            ok = false
          }
          break
        case 'emit':
          if (line == 1) ok = parseEmit(tokens, buildData) else {
            println " Emit specification not second line in $inputFileName"
            ok = false
          }
          break
        case 'work':
          if (!collectProcessed) ok = parseWork(tokens, buildData) else {
            println "Collect not the last line in $inputFileName"
            ok = false
          }
          break
        case 'collect':
          ok = parseCollect(tokens, buildData)
          collectProcessed = true
          break
        default:
          println "Unrecognised specification in $inputFileName for ${tokens[0]}"
          break
      } // switch
      line = line + 1
    }
    // check all specified IP addresses for nodes are unique
    ok = checkIPUniqueness(buildData)
    if (ok) {
      File outFile = new File(outputTextFile)
      PrintWriter printWriter = outFile.newPrintWriter()
      buildData.each { printWriter.println "$it" }
      printWriter.flush()
      printWriter.close()
      File outObjFile = new File(outObjectFile)
      ObjectOutputStream outStream = outObjFile.newObjectOutputStream()
      buildData.each { outStream << it }
      outStream.flush()
      outStream.close()
      println "Parsing completed - no errors in $inputFileName"
    } else println "Parsing failed, see errors highlighted above in $inputFileName"
    return ok
  } // parse()

  boolean checkIPUniqueness (List<ParseRecord> buildData){
    List <String> usedIPs = []
    buildData.each {record ->
      if ( record.fixedIPAddresses != null)
        usedIPs = usedIPs + record.fixedIPAddresses
    }
    int totalSize = usedIPs.size()
    if (totalSize != usedIPs.toUnique().size()){
      println "The specified IPs are not unique $usedIPs"
      return false
    }
    else
      return true
  } // checkIPUniqueness
  boolean parseHost(List tokens, List<ParseRecord> buildData) {
    Integer tokenIndex
    int tokenSize
    tokenSize = tokens.size()
    if ( (tokenIndex = findOption(tokens, tokenSize, "-ip")) == null){
      println "Host -ip option missing from $tokens"
      return false
    }
    else{
      ParseRecord pr = new ParseRecord()
      pr.typeName = tokens[0]
        hostAddress = tokens[tokenIndex + 1]
        pr.hostAddress = hostAddress
        buildData << pr
        return true
    }
  } // parseHost

  boolean parseEmit(List tokens, List<ParseRecord> buildData) {
    Integer tokenIndex
    int tokenSize
    tokenSize = tokens.size()
    tokenIndex = findOption(tokens, tokenSize, "-n")
    if (tokenIndex == null) {
      println "-n(odes) option missing in Emit specification: $tokens"
      return false
    }
    else { // expecting workers option
      ParseRecord pr = new ParseRecord()
      pr.typeName = tokens[0]
      pr.nodes = Integer.parseInt(tokens[tokenIndex + 1])
      if ((tokenIndex = findOption(tokens, tokenSize, "-w")) == null){
        println "-w(orkers) option missing in Emit specification: $tokens"
        return false
      }
      else { // expecting class option
        pr.workers = Integer.parseInt(tokens[tokenIndex + 1])
        if ((tokenIndex = findOption(tokens, tokenSize, "-c")) == null){
          println "-c(lass) option missing in Emit specification: $tokens"
          return false
        }
        else {
          pr.classNameString = tokens[tokenIndex + 1]
          buildData << pr
        } //class
      } //workers
    } // nodes
    // remaining options are not mandatory
    return true
  } // parseEmit

  boolean parseWork(List tokens, List<ParseRecord> buildData) {
    Integer tokenIndex
    int tokenSize
    tokenSize = tokens.size()
    tokenIndex = findOption(tokens, tokenSize, "-ip")
    println "Work nyi"
    return true

  } //parseWork

  boolean parseCollect(List tokens, List<ParseRecord> buildData) {
    Integer tokenIndex
    int tokenSize
    tokenSize = tokens.size()
    tokenIndex = findOption(tokens, tokenSize, "-ip")
    println "Collect nyi"
    return true

  } //parseCollect

} //DSLParser class
