package TestObjects

import org.junit.Test

class ResultCheck {
  @Test
  public void test(){
    int testNumber = 11
    int collectors = 4
    List <String> fileNames = []
    for ( c in 0 ..< collectors) fileNames << "./Test${testNumber}results${c}.dsl4ccout"
    println "Files are $fileNames"
    fileNames.each { String name ->
      println "$name"
      List retrieved = []
      File objFile = new File(name)
      objFile.withObjectInputStream { inStream ->
        inStream.eachObject {
          assert ((it.value > 500) && (it.value <= 900)): "Value ${it.value} out of range"
//          println "${it.value}"
          retrieved << it.value
        }
        inStream.close()
      } // inStream
      println "retrieved: $retrieved"
    } // filenames
  } // test
}
