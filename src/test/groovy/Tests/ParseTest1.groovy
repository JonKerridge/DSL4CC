package Tests

import dsl4cc.DSLparse.DSLParser
import org.junit.Test

class ParseTest1 {
  @Test
  public void test(){
    String workingDirectory = System.getProperty("user.dir")
    DSLParser parser = new DSLParser("$workingDirectory/src/test/groovy/ParserTestFiles/test1")
    assert parser.parse() :"Parsing failed"

  }
}
