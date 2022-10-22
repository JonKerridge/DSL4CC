package dsl4cc.DSLprocesses

import dsl4cc.DSLrecords.Acknowledgement
import dsl4cc.DSLrecords.ParseRecord
import dsl4cc.DSLrecords.TerminalIndex
import groovy_jcsp.ChannelInputList
import groovy_jcsp.ChannelOutputList
import jcsp.lang.CSProcess
import jcsp.lang.Channel
import jcsp.lang.One2OneChannel
import jcsp.lang.ProcessManager
import jcsp.net2.NetAltingChannelInput
import jcsp.net2.NetChannel
import jcsp.net2.Node
import jcsp.net2.mobile.CodeLoadingChannelFilter
import jcsp.net2.tcpip.TCPIPNodeAddress

class DSL4CC_Host implements CSProcess {

  String hostIP
  int requiredManagers
  int totalNodes, totalWorkers
  List<ParseRecord> structure
  def emitObject

  /**
   *
   * @param hostIP the IP address of the host node
   * @param requiredManagers the number of manager processes also running on host node
   * @param totalNodes the total number of nodes required to implement the processing structure
   * @param structure List of ParseRecord describing the infrastructure to be created
   */

  DSL4CC_Host(String hostIP,
              int requiredManagers,
              int totalNodes,
              int totalWorkers,
              List<ParseRecord> structure,
              def emitObject) {
    this.hostIP = hostIP
    this.requiredManagers = requiredManagers
    this.totalNodes = totalNodes
    this.totalWorkers = totalWorkers
    this.structure = structure
    this.emitObject = emitObject
  }

  @Override
  void run() {
    // created in Phase 1
    ChannelOutputList hostToNodes     // writes to node[i] fromHost Node vcn = 1
    NetAltingChannelInput fromNodes   // reads from all nodes toHost Host vcn = 1

//    println "Host starting -> needing $totalNodes nodes and $requiredManagers manager processes"
    // SETUP PHASE 1 create host node and its input channel then start the node processes
    // read in IP addresses of nodes using the fromNodes channel,
    // check that pre-allocated nodes have been started, if not exit the program, otherwise
    // create hostToNode channels and send structure to each node

//    def hostNodeAddress = new TCPIPNodeAddress( 1000)  // find most global IP address available
    String testNodeIP = '127.0.0.1'  //testing only
    def hostNodeAddress = new TCPIPNodeAddress(testNodeIP, 1000) // testing using 127.0.0.1
    Node.getInstance().init(hostNodeAddress)
    String nodeIP = hostNodeAddress.getIpAddress()
    assert hostIP == nodeIP: "Expected hostIP: $hostIP does not match actual nodeIP $nodeIP"
    // can remove the host element from structure
    structure.remove(0)
//    println "Active structure is $structure"
    fromNodes = NetChannel.numberedNet2One(1)

    println "Please start $totalNodes nodes with $nodeIP as host node; creating $totalWorkers internal processes"

    List<String> nodeIPstrings = []
    // assumes nodes have created the corresponding net input channels
    for (n in 1..totalNodes) nodeIPstrings << fromNodes.read() as String
    println "Node IPs are $nodeIPstrings"

    // create the hostToNodes channels

    hostToNodes = []
    nodeIPstrings.each { nodeIPString ->
      def nodeAddress = new TCPIPNodeAddress(nodeIPString, 1000)
      def toNode = NetChannel.one2net(nodeAddress, 1,
        new CodeLoadingChannelFilter.FilterTX())
      hostToNodes.append(toNode)
    }
//    println "Host to Node Channels created"

    // find from structure those nodes that are allocated to fixed IPs
    List preAllocatedIPs = []
    structure.each { record -> if (record.fixedIPAddresses != []) preAllocatedIPs = preAllocatedIPs + record.fixedIPAddresses
    }
//    println "PreAllocated nodes is $preAllocatedIPs"
    // now check that all the preAllocatedIPs have actually been started
    boolean preAllocated = true
    preAllocatedIPs.each { ip ->
      if (!nodeIPstrings.contains(ip)) {
        println "Preallocated node $ip not in node IPs that have started"
        preAllocated = false
      } else nodeIPstrings.remove(ip)
    }
    // if not all pre-allocated nodes have been started exit ungracefully
    if (!preAllocated) {
      println "Host terminating early - all the required pre-allocated nodes have not been started"
      println "The node processes will be terminated automatically with status -2"
      for (n in 0..<totalNodes) hostToNodes[n].write(new TerminalIndex(-2))
      System.exit(-2)
    }
    // know that all the preAllocated nodes can be used as is
    // and that remaining nodes can be used in any way
    // or that no nodes were preAllocated
    structure.each { record ->
      record.allocatedNodeIPs = []
      if (record.fixedIPAddresses == []) {
        // allocate from remaining nodes in nodeIPstrings
        for (n in 1..record.nodes) record.allocatedNodeIPs << nodeIPstrings.pop()
      } else {
        // transfer fixed IPs to allocated
        for (n in 1..record.nodes) record.allocatedNodeIPs << record.fixedIPAddresses.pop()
      }
    } // pre-allocated nodes have been assigned

    // create requestWork and requestIndex channels for each manager
    ChannelInputList requestWorkList = new ChannelInputList()
    ChannelInputList requestIndexList = new ChannelInputList()
    int requestVCN = 2
    for ( m in 0 ..< requiredManagers){
      requestIndexList.append(NetChannel.numberedNet2One(requestVCN))
      requestVCN++
      requestWorkList.append(NetChannel.numberedNet2One(requestVCN))
      requestVCN++
    }

    // update the manager channel numbers
    int structureMax = structure.size() - 1
    // now process rest of structure to add manager channel numbers
    // emit cluster will have output manager at 2 only
    structure[0].outputManagerLocation = requestIndexList[0].getLocation()  //structure[0] is emit cluster
    structure[0].inputManagerLocation = null // not required
    for (i in 1..<structureMax) {
      structure[i].inputManagerLocation = requestWorkList[i-1].getLocation()
      structure[i].outputManagerLocation = requestIndexList[i].getLocation()
    }// for i
    // the last, collect, cluster will have input manager only
    structure[structureMax].inputManagerLocation = requestWorkList[structureMax-1].getLocation()
    structure[structureMax].outputManagerLocation = null // not required
    // now print the updated structure , testing only
    structure.each { println "$it" }
    // now send structure to each node
    for (n in 0..<totalNodes) hostToNodes[n].write(structure)
    // now wait for Nodes to respond
    List<Acknowledgement> acks
    acks = []
    for (n in 1..totalNodes) acks << (fromNodes.read() as Acknowledgement)
    acks.each { ack -> assert ack.ackValue == 1: "Expecting ack value 1 got ${ack.ackValue} from ${ack.ackString}"
    }

    // continue with initialisation  tell nodes phase 2 can start
    acks = []
    for (n in 0..<totalNodes) hostToNodes[n].write(new Acknowledgement(2, hostIP))
//    println "Host has initiated phase 2"
    // and wait for phase 2 to end
    acks = []
    for (n in 1..totalNodes) acks << (fromNodes.read() as Acknowledgement)
    acks.each { ack -> assert ack.ackValue == 2: "Expecting ack value 2 got ${ack.ackValue} from ${ack.ackString}"
    }

//    println "Host starting phase 3 - create Manager processes"
    // SETUP PHASE 3 host can create Manager processes while nodes create their internal channels


    List<ProcessManager> pManagers = []
    One2OneChannel[] manager2host = Channel.one2oneArray(requiredManagers)
    One2OneChannel[] host2manager = Channel.one2oneArray(requiredManagers)
    for (m in 0..<requiredManagers) {
      pManagers[m] = new ProcessManager(new DSL4CC_Manager(fromHost: host2manager[m].in(),
          toHost: manager2host[m].out(),
          requestIndex: requestIndexList[m],
          requestWork: requestWorkList[m],
          managerID: m,
          hostIP: hostIP,
          structure: structure))
    }
    // now start the managers in parallel
    for (m in 0..<requiredManagers) pManagers[m].start()
//    println "$requiredManagers manager processes have started"
    for (m in 0..<requiredManagers) host2manager[m].out().write(new Acknowledgement(3, hostIP))
    println "Sent phase 3 starts to Managers"
    acks = []
    for (m in 0..<requiredManagers) acks << (manager2host[m].in().read() as Acknowledgement)
    acks.each {
      assert it.ackValue == 3: "Expected ackValue of 3 got ${it.ackValue}, ${it.ackString}"
    }
//    println "Host - managers have finished Phase 3 - starting 4"
    for (m in 0..<requiredManagers) host2manager[m].out().write(new Acknowledgement(4, hostIP))
    for (n in 0..<totalNodes) hostToNodes[n].write(new Acknowledgement(4, hostIP))
    println "Host has initiated phase 4"  // involves interactions between nodes and managers
    acks = []
    for (m in 0..<requiredManagers) acks << (manager2host[m].in().read() as Acknowledgement)
    acks.each {
      assert it.ackValue == 4: "Expected ackValue of 4 got ${it.ackValue}, ${it.ackString}"
    }
    acks = []
    for (n in 1..totalNodes) acks << (fromNodes.read() as Acknowledgement)
    acks.each { ack -> assert ack.ackValue == 4: "Expecting ack value 4 got ${ack.ackValue} from ${ack.ackString}"
    }
    println "Host has finished phase 4"
    // start phase 5
    for (n in 0..<totalNodes) hostToNodes[n].write(new Acknowledgement(5, hostIP))
    println "Host has initiated phase 5"  // involves interactions between nodes and managers
    // all the internal processes will have started
    // host expects an acknowledgement from each worker process with ack value 6
    acks = []
    for (n in 1..totalWorkers) acks << (fromNodes.read() as Acknowledgement)
    acks.each { ack -> assert ack.ackValue == 6: "Expecting ack value 6 got ${ack.ackValue} from ${ack.ackString}"
    }
    println "Host has received acknowledgments from all internal processes"
    int currentNode
    currentNode = 0
    for ( s in 0 .. structureMax) {
      for (n in 0..<structure[s].nodes) {
        for (w in 0..<structure[s].workers) {
//          println "Host acking $s, $n, $w, $currentNode"
          hostToNodes[currentNode].write(new Acknowledgement(6, hostIP))
        }
        currentNode++
      }
    }
    println "Host has completed phase 6"  // involves interactions between host and workers

    // waiting for managers to terminate
    for (rm in 0..<requiredManagers) {
      def fh = manager2host[rm].in().read() as Acknowledgement
      assert fh.ackValue == 5: "Host expecting 5 got ${fh.ackValue} from ${fh.ackString}"
    }
    println "Host Terminated"
  }
}
