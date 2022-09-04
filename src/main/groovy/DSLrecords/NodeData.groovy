package DSLrecords

class NodeData implements Serializable{
  String clusterType, methodName, parameterString
  int workers
  int writerManagerChannel
  int readerManagerChannel
  int index

  String toString() {
    return "$clusterType, m: $methodName, ps: $parameterString, w: $workers, wm: $writerManagerChannel, rm: $readerManagerChannel, i: $index"
  }
}
