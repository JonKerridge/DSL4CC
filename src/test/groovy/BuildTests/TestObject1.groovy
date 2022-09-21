package BuildTests

class TestObject1 implements Serializable{
  int value

  TestObject1( int value){
    this.value = value
//    println "Created TO1: $value"
  }

  TestObject1(){
    this.value = 0
  }

  void updateMethod(List params){
    value = value + (params[0] as int)
//    println "Updated TO1 from $initValue to $value"
  }

  static List results = []
  void collect(List params){
    results << value  //test1
//    results << value - params[0]  //test2
//    println "Final TO1 value is $value"
  }

  static void finalise(List params){
//    println "Results= $results"
    List testSet = []
    for ( i in params[0] .. params[1]) {
      testSet << i
      assert results.contains(i): "Value $i missing from collected results"
    }
    println "All expected values were present"

    results.each{ int v ->
      assert testSet.contains(v):"Value $v fails the finalise check"
    }
    println "Correctness test validated"
  }


  String toString(){
    return "TO1: $value"
  }
}
