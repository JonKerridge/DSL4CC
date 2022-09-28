package BuildTests

import dsl4cc.DSLbuild.DSLBuild
import org.junit.Test

import static org.junit.Assert.assertTrue

class Test2 {
  @Test
  public void test(){

    String fileToBuild = "D:\\IJGradle\\DSL4CC\\src\\test\\groovy\\ParserTestFiles\\test2.dsl4ccstruct"

    def creator = new DSLBuild(structureFile: fileToBuild)

    assertTrue ( creator.builder() )
  }
}
