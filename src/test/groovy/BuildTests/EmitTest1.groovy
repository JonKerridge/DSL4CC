package BuildTests

import DSLrecords.EmitInterface
import DSLrecords.EmittedObject

class EmitTest1 implements  EmitInterface <TestObject1> {
  int instances, currentValue

  EmitTest1 (List params){
    currentValue = 0
    instances = params[0] as int
  }

  @Override
  EmittedObject <TestObject1> create() {
    def eo = new EmittedObject()
    if (currentValue < instances){
      eo.emittedObject = new TestObject1(currentValue)
      currentValue++
      eo.valid = true
    }
    else {
      eo.valid = false
    }
    return eo
  }
}
