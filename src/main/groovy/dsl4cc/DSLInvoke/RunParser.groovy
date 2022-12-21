package dsl4cc.DSLInvoke

import dsl4cc.DSLparse.DSLParser

class RunParser {
  static void main(String[] args) {
    String workingDirectory = System.getProperty("user.dir")
    DSLParser parser = new DSLParser("$workingDirectory/src/test/groovy/ParserTestFiles/test1")
    assert parser.parse() :"Parsing failed"
  }
}
