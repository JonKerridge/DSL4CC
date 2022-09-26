package BuildTests

import DSLbuild.DSLBuild
import org.junit.Test

import static org.junit.Assert.assertTrue

class Test4 {
  @Test
  public void test(){

    String fileToBuild = "D:\\IJGradle\\DSL4CC\\src\\test\\groovy\\ParserTestFiles\\test4.dsl4ccstruct"

    def creator = new DSLBuild(structureFile: fileToBuild)

    assertTrue ( creator.builder() )
  }
}
