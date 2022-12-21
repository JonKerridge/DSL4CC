package Tests

import dsl4cc.DSLbuild.DSLBuilder
import org.junit.Test
import TestObjects.*

import static org.junit.Assert.assertTrue

class BuildTest1 {
  @Test
  public void test(){

    String fileToBuild = "D:\\IJGradle\\DSL4CC\\src\\test\\groovy\\ParserTestFiles\\test1"

    def creator = new DSLBuilder(fileToBuild, TestObject, CollectObject)

    assertTrue ( creator.builder() )
  }
}
