package BuildTests

class TestObject3 implements Serializable{
  int value

  TestObject3(int value){
    this.value = value
//    println "Created TO1: $value"
  }

  TestObject3(){}

  void updateMethod(List params){
    value = value + params[0]
  }

  void collect(List params){
    value = value - params[0]  //test2
  }

  void finalise(List params){
  }

  String toString(){
    return "TO3: $value"
  }
}
