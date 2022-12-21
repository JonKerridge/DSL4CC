package dsl4cc.DSLrecords

class Acknowledgement implements Serializable{
  int ackValue
  String ackString
//  List <TerminalIndex> terminalRequests

  Acknowledgement (int value, String string){
    ackValue = value
    ackString = string
//    this.terminalRequests = null
  }

//  Acknowledgement (int value, String string, List <TerminalIndex> terminalRequests){
//    ackValue = value
//    ackString = string
//    this.terminalRequests = terminalRequests
//  }

  String toString(){
    return "ACK value: $ackValue, string: $ackString"
  }
}
