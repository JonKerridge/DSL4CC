package BuildTests

import DSLrecords.EmitInterface
import DSLrecords.EmittedObject

class EmitTest2 implements  EmitInterface <TestObject1> {
  int initialValue, finalValue, currentValue

    EmitTest2(List params){
    finalValue = params[1] as int
    initialValue = params[0] as int
      currentValue = initialValue
    println "Emitting from $initialValue to $finalValue"
  }

  @Override
  EmittedObject <TestObject1> create() {
    def eo = new EmittedObject()
    if (currentValue <= finalValue){
      TestObject1 tObj = new TestObject1(currentValue)
      println "$tObj"
      eo.emittedObject = tObj
      currentValue++
      eo.valid = true
    }
    else {
      eo.valid = false
    }
    return eo
  }
}
