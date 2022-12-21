package TestObjects

import dsl4cc.DSLrecords.EmitInterface

class TestObject implements EmitInterface<TestObject>, Serializable{
  int value
  int currentValue, finalValue

  TestObject(List params){
    this.currentValue = params[0] as int
    this.finalValue = params[1] as int
  }

  TestObject(int value){
    this.value = value
//    println "Created TO1: $value"
  }

  void updateMethod(List params){
    value = value + (params[0] as int)
//    println "Updated TO1 from $initValue to $value"
  }

  @Override
  TestObject create() {
    if (currentValue < finalValue){
      currentValue++
      return new TestObject(currentValue)
    }
    else
      return null
  }

  String toString(){
    return "TO: $value"
  }
}
