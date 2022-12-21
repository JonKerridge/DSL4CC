package dsl4cc.DSLprocesses

import dsl4cc.DSLrecords.Acknowledgement
import dsl4cc.DSLrecords.ChannelData
import dsl4cc.DSLrecords.ParseRecord
import dsl4cc.DSLrecords.RequestSend
import dsl4cc.DSLrecords.TerminalIndex
import groovy_jcsp.ALT
import groovy_jcsp.ChannelOutputList
import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.lang.ChannelOutput
import jcsp.net2.NetAltingChannelInput
import jcsp.net2.NetChannel
import jcsp.net2.NetChannelLocation
import jcsp.net2.NetChannelOutput
import jcsp.net2.tcpip.TCPIPNodeAddress

class DSL4CC_Manager implements CSProcess{

  ChannelInput fromHost
  ChannelOutput toHost
  int managerID  //gives the manager subscript allocated in order from zero up
  String hostIP
  List <ParseRecord> structure

  // created by host in phase 3 and passed as properties of the class
  NetAltingChannelInput requestWork
  NetAltingChannelInput requestIndex

  // created in phase 4
  NetChannelOutput toNode               // connects to fromManager
  ChannelOutputList outputTermination
  ChannelOutputList useIndex

  @Override
  void run() {
    int outStructure, inStructure
    int outNodes, outWorkers, inNodes, inWorkers
    int requestWorkChannelNumber, requestIndexChannelNumber
//    println "Manager $managerID has started in phase 3"
    Acknowledgement ack
    ack = fromHost.read() as Acknowledgement
    assert ack.ackValue == 3 : "Manager $managerID expected phase 3 got ${ack.ackValue}"
    // SETUP PHASE 3 create the Manager infrastructure
//    structureSize = structure.size()
    outStructure = managerID
    outNodes = structure[outStructure].nodes
    outWorkers = structure[outStructure].workers
//    requestIndexChannelNumber = structure[outStructure].outputManagerLocation
    inStructure = managerID + 1
    inNodes = structure[inStructure].nodes
    inWorkers = structure[inStructure].workers
//    requestWorkChannelNumber = structure[inStructure].inputManagerLocation
//    requestWork = NetChannel.numberedNet2One(requestWorkChannelNumber)
//    requestIndex = NetChannel.numberedNet2One(requestIndexChannelNumber)
//    println "Manager $managerID has created channels at vcn index= $requestIndexChannelNumber, work= $requestWorkChannelNumber "
//    println "Manager $managerID sending response to host ending phase 3"
    ack.ackString = "Manager $managerID"
    toHost.write(ack)

    // SETUP PHASE 4 Manager can extract data about connected nodes
    // and input data from the nodes
    ack = fromHost.read() as Acknowledgement
    assert ack.ackValue == 4 : "Manager $managerID expected phase 4 got ${ack.ackValue}"
//    println "Manager $managerID starting phase 4"

    List <ChannelData> workInList, sendToList
    workInList = []
    sendToList = []
    // so manager waits to read from nodes
    for ( i in 0 ..< outNodes) sendToList << (requestIndex.read() as ChannelData)
    for ( i in 0 ..< inNodes) workInList << (requestWork.read() as ChannelData)
//    println "Manager $managerID sendToList = $sendToList"
//    println "Manager $managerID workInList = $workInList"
    // the workInList can be sent to the Nodes that have an output end processed by this manager
    for ( i in 0 ..< outNodes){
      String outNodeIP = structure[outStructure].allocatedNodeIPs[i]
//      println "Manager $managerID sending workInlist to $outNodeIP"
      TCPIPNodeAddress outNodeAddress = new TCPIPNodeAddress( outNodeIP, 1000)
      toNode = NetChannel.one2net(outNodeAddress, 2)
      toNode.write(workInList)
    }
//    println "Manager $managerID has sent workInList to connected out-end nodes "
    // Manager can create net channels to the workInList channels
    // so that it can send termination objects to each of the Workers once
    // Workers that out put data have terminated
    outputTermination = new ChannelOutputList()
    workInList.sort new OrderBy([{it.nodeIndex}])
    workInList.each { cd ->
      String type = cd.channelType
      int terminalWorkers = cd.chanLocation.size()
      assert type == "inputWork": "Manager $managerID expected ChannelData of type inputWork got $type"
      for (w in 0..< terminalWorkers){
//          println "Manager $managerID has outputTermination[$w] = ${cd.chanLocation[w]}"
        outputTermination.append(NetChannel.one2net(cd.chanLocation[w] as NetChannelLocation))
      }
    }
//    println "Manager $managerID has created outputTermination channels"
//    for ( w in 0 ..< outputTermination.size())
//      println "Manager $managerID outTerm[$w] = ${outputTermination[w].getLocation()}"


    // list elements could have been read in random order so need to sort
    // the sendTo list which is used to construct a ChannelOutputList in the Manager
    useIndex = new ChannelOutputList()
    // useIndex is outNodes * outWorkers long
//    println "Manager $managerID has useIndex $outNodes * $outWorkers entries"
    sendToList.sort new OrderBy([{it.nodeIndex}])
    sendToList.each{cd ->
      String type = cd.channelType
      assert type == "sendTo":"Manager $managerID expected ChannelData of type sendTo got $type"
      for ( w in 0 ..< outWorkers) {
//        println "Manager $managerID has useIndex[$w] = ${cd.chanLocation[w]}"
        useIndex.append( NetChannel.one2net(cd.chanLocation[w] as NetChannelLocation))
      }
    }
//    for ( w in 0 ..< (outWorkers *outNodes))
//      println "Manager $managerID useIndex[$w] = ${useIndex[w].getLocation()}"
//    println "Manager $managerID has created its useIndex ChannelOutputList and has finished phase 4"
    ack.ackString = "Manager $managerID"
    ack.ackValue = 4
    toHost.write(ack)  // ending phase 4
//    println "Manger $managerID ready to operate its internal queue having finished phase 4"
    List <RequestSend> requests = []  // holds indexes of workers making input requests
//    List <TerminalIndex> terminalRequests = [] // to hold TerminalIndex record of workers that have terminated
    int terminalCount = 0
//    int totalOutWorkers = outNodes * outWorkers
    boolean running = true
    def managerAlt = new ALT([requestWork, requestIndex])
    boolean [] preCon = new boolean[2]
    preCon[0] = true  // can always make a request for work input
    preCon[1] = false // can only make an index request when there is something in requests
    while (running) {
      switch (managerAlt.priSelect(preCon)) {
        case 0 : // a request to input work
          requests << (requestWork.read() as RequestSend)
          break
        case 1: // a request for an index or a termination signal from a node
          def inputObject = requestIndex.read()
          if (inputObject instanceof TerminalIndex) {
//            println "Manager $managerID is processing termination for $outNodes workers, " +
//                "currently $terminalCount have terminated"
//            TerminalIndex terminalIndex = inputObject as TerminalIndex
//            terminalRequests << terminalIndex
            terminalCount++
            if (terminalCount == outNodes) running = false
          }
          else {
            useIndex[(inputObject  as RequestSend).index].write(requests.pop())
          }
          break
      } // end switch
      preCon[1] = (requests.size() > 0)
//      println "Manager $managerID has requests from $requests"
    } // running loop

    // start termination procedure
    // read request for work from those workers that have not yet
    // sent a request during the termination phase
    int totalInWorkers = inNodes * inWorkers
    int initialSize = requests.size()
//    println "Manager $managerID starting termination:: $totalInWorkers, $initialSize"
    for ( w in initialSize ..< totalInWorkers) requests << (requestWork.read() as RequestSend)
    // now terminate each of the inputting workers
//    println "Manager $managerID has all inputting workers requests"
    for (w in 0 ..< totalInWorkers)
      outputTermination[w].write(new TerminalIndex("Manager $managerID inWorker: ", w, 0))

    // now tell host manager has terminated
//    println "Manager $managerID now informing Host of termination"
    toHost.write(new Acknowledgement(5, "Manager $managerID"))
    println "Manager $managerID terminated"
  }// run()
}
