package ParserTests

import DSLparse.DSLParser
import org.junit.Test

import static org.junit.Assert.assertTrue

class ParseTest1a {

  @Test
  public void test(){

    String fileToParse = "D:\\IJGradle\\DSL4CC\\src\\test\\groovy\\ParserTestFiles\\test1a.dsl4cc"

    def parser = new DSLParser(fileToParse)

    assertTrue ( parser.parse() == true)
  }
}
