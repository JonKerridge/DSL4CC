package BuildTests

import DSLbuild.DSLBuild
import DSLparse.DSLParser
import org.junit.Test

import static org.junit.Assert.assertTrue

class Test1 {
  @Test
  public void test(){

    String fileToBuild = "D:\\IJGradle\\DSL4CC\\src\\test\\groovy\\ParserTestFiles\\test1.dsl4ccstruct"

    def creator = new DSLBuild(structureFile: fileToBuild)

    assertTrue ( creator.builder() )
  }
}
