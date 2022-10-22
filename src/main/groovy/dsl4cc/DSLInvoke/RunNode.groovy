package dsl4cc.DSLInvoke

import dsl4cc.DSLprocesses.DSL4CC_Node
import groovy_jcsp.PAR

class RunNode {
  static void main(String [] args){
    String hostIP, testNodeIP
    DSL4CC_Node node
    hostIP = args[0]
    if ( args.size() > 1) {
      testNodeIP = args[1]
      node = new DSL4CC_Node(hostIP: hostIP, testingNodeIP: testNodeIP)
    }
    else
      node = new DSL4CC_Node(hostIP: hostIP)

    new PAR([node]).run()
  }
}
