package BuildTests

import DSLprocesses.DSL4CC_Node
import groovy_jcsp.PAR

String hostIP = "127.0.0.1"
String testIP = "127.0.0.6"  // only required when testing

def node = new DSL4CC_Node(hostIP: hostIP, testingNodeIP: testIP)

new PAR([node]).run()