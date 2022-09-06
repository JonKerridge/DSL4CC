package BuildTests

class TestObject1 implements Serializable{
  int value

  TestObject1( int value){
    this.value = value
//    println "Created TO1: $value"
  }

  void updateMethod(List params){
    value = value + params[0]
//    println "Updated TO1 from $initValue to $value"
  }

  static List results = []
  void collect(List params){
    results << value  //test1
//    results << value - params[0]  //test2
//    println "Final TO1 value is $value"
  }

  void finalise(List params){
//    println "Results= $results"
    for ( i in params[0] ..< params[1])
      assert results.contains(i):"Value $i missing from collected results"
    println "All expected values were present"
  }


  String toString(){
    return "TO1: $value"
  }
}
