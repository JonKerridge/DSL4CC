package TestObjects

import dsl4cc.DSLrecords.CollectInterface

class CollectObject implements CollectInterface <TestObject>{

  int sum, numbers
  float mean

  CollectObject(){
    sum = 0
    numbers = 0
  }

  @Override
  void collate(TestObject data, List params) {
    sum = sum + data.value
    numbers++
  }

  @Override
  void finalise(List params) {
    mean = (sum as float) / (numbers as float)
    println "$numbers entries had $sum total and mean of $mean"
  }
}
