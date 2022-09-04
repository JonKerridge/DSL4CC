package BuildTests

import DSLbuild.DSLBuild
import org.junit.Test

import static org.junit.Assert.assertTrue

class Test1a {
  @Test
  public void test(){

    String fileToBuild = "D:\\IJGradle\\DSL4CC\\src\\test\\groovy\\ParserTestFiles\\test1a.dsl4ccstruct"

    def creator = new DSLBuild(structureFile: fileToBuild)

    assertTrue ( creator.builder() )
  }
}
