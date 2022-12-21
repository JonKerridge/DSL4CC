package dsl4cc.DSLrecords

class TimingData implements  Serializable{
  String nodeIP, nodeType, methodName
  int nodeIndex
  Long nodeElapsed
  Map <Integer, Long> processingTimes

  String toString(){
    List <Integer> sortedTimesKeys
    String s
    s = "$nodeIP, $nodeIndex, $nodeType, $methodName, $nodeElapsed, ,"
    sortedTimesKeys = processingTimes.sort()*.key
    sortedTimesKeys.each {Integer k->
      s = s + "$k, ${processingTimes.get(k)}, "
    }
    return s
  }

}
