package BuildTests

import dsl4cc.DSLrecords.EmitInterface
import dsl4cc.DSLrecords.EmittedObject

class EmitTest3 implements  EmitInterface <TestObject3> {
  int initialValue, finalValue, currentValue

  EmitTest3(List params){
    finalValue = params[1] as int
    initialValue = params[0] as int
    currentValue = initialValue
//    println "Emitting from $initialValue to $finalValue"
  }

  @Override
  EmittedObject <TestObject3> create() {
    def eo = new EmittedObject()
    if (currentValue <= finalValue){
      TestObject3 tObj = new TestObject3(currentValue)
//      println "$tObj"
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
