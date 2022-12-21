package dsl4cc.DSLInvoke

import dsl4cc.DSLprocesses.DSL4CC_Node
import groovy_jcsp.PAR

class RunNode {
  String hostIP, testNodeIP
  DSL4CC_Node node

  RunNode(String hostIP, String testNodeIP){
    this.hostIP = hostIP
    this.testNodeIP = testNodeIP
    this.node = new DSL4CC_Node(hostIP: hostIP, testingNodeIP: testNodeIP)
  }

  RunNode(String hostIP){
    this.hostIP = hostIP
    this.node = new DSL4CC_Node(hostIP: hostIP)
  }

  void invoke(){
    new PAR([node]).run()
  }

  static void main(String [] args){
    DSL4CC_Node node
    switch (args.size()){
      case 1:
        node = new RunNode( args[0]).node
        break
      case 2:
        node = new RunNode( args[0], args[1]).node
        break
      default:
        node = null
        println "RunNode usage" +
            "\n new dsl4cc.DSLInvoke.RunNode( args[0])" +
            "\n new dsl4cc.DSLInvoke.RunNode( args[0], args[1])" +
            "\n\twhere " +
            "\n\t\targs[0] is the host ip address and the node's ip determined automatically" +
            "\n\t\targs[1] is the ip address of the node being created, " +
            "\n\t\t\tused for testing and will be of form 127.0.0.?" +
            "\n\t\t\tand host ip (args[0]) will typically be 127.0.0.1"
        break
    } //switch
    if (node != null)
      new PAR([node]).run()
  } //main
}
