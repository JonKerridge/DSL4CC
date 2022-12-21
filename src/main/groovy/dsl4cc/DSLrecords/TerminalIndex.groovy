package dsl4cc.DSLrecords

class TerminalIndex implements Serializable{
  String nodeIP
  int tIndex  // the index of the Emitter,Worker, Collector or Node that has terminated
  long elapsed
//  long nodeTime, collectorTime  removed when sending timing data back ina different way
//  List <Long> collectTimes

  TerminalIndex(String node, int index, long elapsed){
    this.nodeIP = node
    this.tIndex = index
    this.elapsed = elapsed
//    this.nodeTime = -1
//    this.collectorTime = -1
//    this.collectTimes = null
  }
//  TerminalIndex(String node, int index, long nodeTime){
//    this.nodeIP = node
//    this.tIndex = index
//    this.nodeTime = nodeTime
//    this.collectorTime = -1
//    this.collectTimes = null
//  }
//  TerminalIndex(String node, int index, long nodeTime, List <Long> collectTimes){
//    this.nodeIP = node
//    this.tIndex = index
//    this.nodeTime = nodeTime
//    this.collectorTime = -1
//    this.collectTimes = collectTimes
//  }
//  TerminalIndex(String node, int index, long nodeTime, long collectorTime){
//    this.nodeIP = node
//    this.tIndex = index
//    this.nodeTime = nodeTime
//    this.collectorTime = collectorTime
//    this.collectTimes = null
//  }

  String toString(){
    String s = "\n\tNode ID = $nodeIP " +
        "\n\tIndex = $tIndex " +
        "\n\tElapsed = $elapsed "
//        "\n\tCollector Time = $collectorTime, " +
//        "\n\tCollect Times = $collectTimes"
  }
}
