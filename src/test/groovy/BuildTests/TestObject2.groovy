package BuildTests

class TestObject2 implements Serializable{
  int value

  TestObject2(int value){
    this.value = value
//    println "Created TO1: $value"
  }

  TestObject2(){}

  void updateMethod(List params){
    value = value + params[0]
//    println "Updated TO1 from $initValue to $value"
  }

  static List results = []
  void collect(List params){
//    results << value  //test1
    results << value - params[0]  //test2
//    println "Final TO1 value is $value"
  }

  void finalise(List params){
//    println "Results= $results"
    results.each { assert ((it <= params[1]) && (it >= params[0])) : "Result $it is not between ${params[0]} and ${params[1]}"}
//    for ( i in params[0] .. params[1])
//      assert results.contains(i):"Value $i missing from collected results"
    println "All expected values were present"
  }


  String toString(){
    return "TO1: $value"
  }
}
