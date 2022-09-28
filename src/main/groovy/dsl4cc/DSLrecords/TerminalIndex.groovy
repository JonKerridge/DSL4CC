package dsl4cc.DSLrecords

class TerminalIndex implements Serializable{
  int tIndex  // the index of the Worker or Node that has terminated

  TerminalIndex(int index){
    this.tIndex = index
  }
}
