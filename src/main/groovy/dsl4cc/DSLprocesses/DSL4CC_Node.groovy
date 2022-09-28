package dsl4cc.DSLprocesses

import dsl4cc.DSLrecords.Acknowledgement
import dsl4cc.DSLrecords.ChannelData
import dsl4cc.DSLrecords.ParseRecord
import dsl4cc.DSLrecords.TerminalIndex
import groovy_jcsp.ChannelInputList
import groovy_jcsp.ChannelOutputList
import jcsp.lang.Any2OneChannel
import jcsp.lang.CSProcess
import jcsp.lang.Channel
import jcsp.lang.ProcessManager
import jcsp.net2.*
import jcsp.net2.tcpip.TCPIPNodeAddress

class DSL4CC_Node implements CSProcess {

  String hostIP
  String testingNodeIP  // only used for testing on 127.0.0.0 network

  @Override
  void run() {
    List<ParseRecord> structure
    //created in phase 1
    NetSharedChannelOutput toHost     // writes to Host: fromNodes
    NetSharedChannelInput fromHost  // reads from Host: hostToNodes[i] vcn = 1
    NetAltingChannelInput fromManager  // reads from node's toManager vcn = 2

    //created in Phase 2
    ChannelInputList inputWork = new ChannelInputList()
    ChannelInputList sendTo = new ChannelInputList()


    //SETUP PHASE 1

    def nodeAddress = new TCPIPNodeAddress(testingNodeIP, 1000)  // remove when not testing
//    def nodeAddress = new TCPIPNodeAddress(1000)
    Node.getInstance().init(nodeAddress)
    String nodeIP = nodeAddress.getIpAddress()
    println "Node $nodeIP has started with host $hostIP"
    fromHost = NetChannel.numberedNet2Any(1) //, new CodeLoadingChannelFilter.FilterRX())
    fromManager = NetChannel.numberedNet2One(2)
    def hostAddress = new TCPIPNodeAddress(hostIP, 1000)
    toHost = NetChannel.any2net(hostAddress, 1)
    toHost.write(nodeIP)
//    println "Node $nodeIP has sent its IP address to host at $hostIP"
    // node can now read in the structure object unless some preAllocated nodes have not been started
    Object dataFromHost = fromHost.read()
    if (dataFromHost instanceof TerminalIndex) System.exit(-2)  // instant termination
    structure = dataFromHost as List<ParseRecord>
//    structure.each {println "$it"}
    Acknowledgement ack
    ack = new Acknowledgement(1, nodeIP)
//    println "sending $ack"
    toHost.write(ack)
    // wait for host to send continue signal
//    println "Node $nodeIP waiting to continue"
    ack = fromHost.read() as Acknowledgement
    assert ack.ackValue == 2: "Node $nodeIP expecting to start setup phase 2, got $ack instead"

    // SETUP PHASE 2 create all the channels used by nodes and workers in the cluster

    // determine the role of this node; search for nodeIP in structure in allocatedIPs
    int nodeIndex, structureIndex, workers
    NetChannelLocation inputManagerLocation, outputManagerLocation
    String nodeType
    nodeIndex = 0 // the subscript of this node within the cluster
    structureIndex = 0

    // the subscript within structure of the cluster in which the node participates
    boolean found
    found = false
    // we know the node must have been allocated so no size checks are required
    while (!found) {
      if (structure[structureIndex].allocatedNodeIPs[nodeIndex] == nodeIP)
        found = true
      else {
        nodeIndex++
        if (nodeIndex == structure[structureIndex].allocatedNodeIPs.size()) {
          structureIndex++
          nodeIndex = 0
        }
      }
    } // while found nodeIndex and structureIndex have the correct values
//    println "Node $nodeIP has ni = $nodeIndex and si = $structureIndex"

    // need to create worker instances of inputWork, and sendTo channels,
    // provided they are required, the respective managers have to be non-zero
    nodeType = structure[structureIndex].typeName
    workers = structure[structureIndex].workers
    inputManagerLocation = structure[structureIndex].inputManagerLocation as NetChannelLocation
    outputManagerLocation = structure[structureIndex].outputManagerLocation as NetChannelLocation

//    println "Node $nodeIP has t = $nodeType, ni = $nodeIndex, w = $workers, om = $outputManagerLocation, im = $inputManagerLocation"


    ChannelData inputLocations = new ChannelData()  // used to send channel locations to nodes
    ChannelData sendToLocations = new ChannelData() // that will then output to these channels

    if (outputManagerLocation != null) { // sendTo used in emit and work types
      sendToLocations.channelType = "sendTo"
      sendToLocations.nodeIndex = nodeIndex
      for (w in 0..<workers) {
        def sendToChan = NetChannel.net2one()
        sendTo.append(sendToChan)
        sendToLocations.chanLocation << sendToChan.getLocation()
      }
    }
    if (inputManagerLocation != null) { // inputWork used in work and collect types
      inputLocations.channelType = "inputWork"
      inputLocations.nodeIndex = nodeIndex
      for (w in 0..<workers) {
        def inputChan = NetChannel.net2one()
        inputWork.append(inputChan)
        inputLocations.chanLocation << inputChan.getLocation()
      }
    }

    // end of phase 2 so inform host ack value is still 2
    ack.ackString = nodeIP
    toHost.write(ack)
//    println "Node $nodeIP finished phase 2"

    // SETUP PHASE 4 - host has set up Managers so node can send channel locations to its manager
    ack = fromHost.read() as Acknowledgement
    assert ack.ackValue == 4: "Node expected start of phase 4 got ${ack.ackValue}"

//    println "Node $nodeIP starting phase 4"

    // create the output net channels to the Manager processes and send channel data
    NetChannelOutput requestWork, requestIndex
    if (outputManagerLocation != null) {
      requestIndex = NetChannel.any2net(outputManagerLocation)
      requestIndex.write(sendToLocations)
//      println "Node $nodeIP written sendTo locations $sendToLocations"
    }
    if (inputManagerLocation != null) {
      requestWork = NetChannel.any2net(inputManagerLocation)
      requestWork.write(inputLocations)
//      println "Node $nodeIP written inputWork locations $inputLocations"
    }
    List<ChannelData> workInList
    ChannelOutputList outputWork = new ChannelOutputList()
    int totalWorkers
    if (outputManagerLocation != null) {
      workInList = (fromManager.read() as List<ChannelData>)
//      println "Node $nodeIP has read its workInList"
      // now construct a ChannelOutputList for outputWork,
      // List entries may not be in order
      // all Workers in all Nodes in Cluster can access the whole ChannelOutputList
      workInList.sort new OrderBy([{ it.nodeIndex }])
      workInList.each { cd ->
        String type = cd.channelType
        int inWorkers = cd.chanLocation.size()
        assert type == "inputWork": "Node $nodeIP expected ChannelData of type inputWork got $type"
        for (w in 0..<inWorkers) {
//          println "Node $nodeIP appending ${cd.chanLocation[w]} to outputWork"
          outputWork.append(NetChannel.one2net(cd.chanLocation[w] as NetChannelLocation))
        }
      }
//      println "Node $nodeIP has processed its workInList"
    }
    ack.ackString = "Node $nodeIP"
    ack.ackValue = 4
    toHost.write(ack)
//    println "Node $nodeIP ready to start the worker processes"
    ack = fromHost.read() as Acknowledgement
    assert ack.ackValue == 5: "Node $nodeIP expecting to start setup phase 5, got $ack instead"
    String activityName
    List<String> parameters
    //emit gets one of a list of list of string for collectParameters collect has a finalise method and params
    if (nodeType == 'emit') activityName = structure[structureIndex].classNameString
    else activityName = structure[structureIndex].methodNameString
    parameters = structure[structureIndex].parameterString
//    println "Node $nodeIP with ni= $nodeIndex starting phase 5 type= $nodeType w= $workers, " + "act= $activityName, p= $collectParameters"
    Any2OneChannel fromWorkers = Channel.any2one()
    List<ProcessManager> nodeProcessManagers = []
    boolean emitWorkNode = true
    switch (nodeType) {
      case 'emit':
        for (w in 0..<workers) {
          int workerID = (nodeIndex * workers) + w
          nodeProcessManagers[w] = new ProcessManager(new DSL4CC_Emitter(
              toHost: toHost,
              fromHost: fromHost,
              requestIndex: requestIndex,
              useIndex: sendTo[w],
              outputWork: outputWork,
              workerToNode: fromWorkers.out(),
              workerID: workerID,
              className: activityName,
              parameters: structure[structureIndex].emitParameterString[workerID]))
        }
        break
      case 'work':
        for (w in 0..<workers) {
          int workerID = (nodeIndex * workers) + w
          nodeProcessManagers[w] = new ProcessManager(new DSL4CC_Worker(
              toHost: toHost,
              fromHost: fromHost,
              requestIndex: requestIndex,
              useIndex: sendTo[w],
              outputWork: outputWork,
              requestWork: requestWork,
              inputWork: inputWork[w],
              workerToNode: fromWorkers.out(),
              workerID: workerID,
              methodName: activityName,
              parameters: parameters))
        }
        break
      case 'collect':
        emitWorkNode = false
        for (w in 0..<workers) {
          int workerID = (nodeIndex * workers) + w
          nodeProcessManagers[w] = new ProcessManager(new DSL4CC_Collecter(
              toHost: toHost,
              fromHost: fromHost,
              requestWork: requestWork,
              inputWork: inputWork[w],
              workerToNode: fromWorkers.out(),
              workerID: workerID,
              outFileName: structure[structureIndex].outFileName,
              collectMethodName: activityName, //collect method and collectParameters
              collectParameters: structure[structureIndex].collectParameterString[workerID],
              finaliseMethodName: structure[structureIndex].finaliseNameString, // finalise method and params
              finaliseParameters: structure[structureIndex].finaliseParameterString[workerID]))
        }
        break
    }  // end switch
    for (w in 0..<nodeProcessManagers.size()) nodeProcessManagers[w].start()

//    println "Node $nodeIP has started its worker processes - awaiting termination"
    for (w in 0..<workers) {
      TerminalIndex terminatedWorker = fromWorkers.in().read() as TerminalIndex
//      int tIndex = terminatedWorker.tIndex
//      assert tIndex <= nodeIndex + w: "Unexpected index ($tIndex) for terminated worker: expecting value less than $workers"
    }
    // all the node's workers have terminated, so inform its manager
//    println "Node $nodeIP - all workers have terminated"
    if (emitWorkNode) {
      TerminalIndex tIndex = new TerminalIndex(nodeIndex)
//      println "Node $nodeIP Terminating with $tIndex"
      //cannot do this in a collect node because the requestIndex channel does not exist
      requestIndex.write(tIndex)
    }
    println "Node $nodeIP Terminated"
  }
}
