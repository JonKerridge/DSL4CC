package Tests

import TestObjects.CollectObject
import TestObjects.TestObject
import dsl4cc.DSLbuild.DSLBuilder
import org.junit.Test

import static org.junit.Assert.assertTrue

class BuildTest11 {
  @Test
  public void test(){

    String fileToBuild = "D:\\IJGradle\\DSL4CC\\src\\test\\groovy\\ParserTestFiles\\test11"

    def creator = new DSLBuilder(fileToBuild, TestObject, CollectObject)

    assertTrue ( creator.builder() )
  }
}
