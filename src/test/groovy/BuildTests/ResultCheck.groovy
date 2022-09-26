package BuildTests

import org.junit.Test

class ResultCheck {
  @Test
  public void test(){
    List retrieved = []
    String fileBase = "D:\\IJGradle\\DSL4CC\\src\\test\\groovy\\BuildTests\\"
    List <String> fileNames = ["TO3results0.dsl4ccout","TO3results1.dsl4ccout","TO3results2.dsl4ccout","TO3results3.dsl4ccout"]
    fileNames.each { String name ->
      println "$name"
      File objFile = new File(fileBase + name)
      objFile.withObjectInputStream { inStream ->
        inStream.eachObject {
          assert ((it.value >= 0) && (it.value <= 199)): "Value ${it.value} out of range"
//          println "${it.value}"
          retrieved << it.value
        }
        inStream.close()
      }
    }
    println "retrieved: ${retrieved.sort()}"


  }
}
