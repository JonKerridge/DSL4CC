package ParserTests

import dsl4cc.DSLparse.DSLParser
import org.junit.Test
import static org.junit.Assert.assertTrue

class ParseTest1 {

  @Test
  public void test(){

    String fileToParse = "D:\\IJGradle\\DSL4CC\\src\\test\\groovy\\ParserTestFiles\\test1.dsl4cc"

    def parser = new DSLParser(fileToParse)

    assertTrue ( parser.parse() == true)
  }
}
