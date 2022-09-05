package BuildTests

import DSLrecords.EmitInterface
import DSLrecords.EmittedObject

class EmitTest1 implements  EmitInterface <TestObject1> {
  int instances, currentValue

  EmitTest1 (List params){
    currentValue = params[1] as int
    instances = params[0] as int
    println "Emitting from $currentValue for $instances instances"
  }

  @Override
  EmittedObject <TestObject1> create() {
    def eo = new EmittedObject()
    if (currentValue < instances){
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
