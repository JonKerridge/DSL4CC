package DSLprocesses

import DSLrecords.ExtractParameters
import DSLrecords.RequestSend
import DSLrecords.TerminalIndex
import groovy_jcsp.ChannelOutputList
import jcsp.lang.CSProcess
import jcsp.lang.ChannelOutput
import jcsp.net2.NetAltingChannelInput
import jcsp.net2.NetChannelOutput
import jcsp.net2.NetSharedChannelInput

class DSL4CC_Worker implements CSProcess{

  // net channel connections to and from Host
  NetChannelOutput toHost     // writes to Host: fromNodes
  NetSharedChannelInput fromHost  // reads from Host: hostToNodes[i] vcn = 1

  // net channels
  ChannelOutputList outputWork
  NetChannelOutput requestWork, requestIndex
  NetAltingChannelInput inputWork, useIndex
  // internal channels to Node
  ChannelOutput workerToNode
  int workerID    // relative to the cluster not the node
  String methodName
  List <String> parameters    // String representations of the parameter values

  @Override
  void run() {
    println "Worker $workerID, $methodName, $parameters "
//    String s =  " "
//    for ( w in 0 ..< outputWork.size())
//      s = s + "\now[$w] = ${outputWork[w].getLocation()}"
//    println "Worker $workerID, $collectMethodName, $collectParameters, " +
//        "\nrw = ${requestWork.getLocation()}, " +
//        "\niw = ${inputWork.getLocation()}," +
//        "\nri = ${requestIndex.getLocation()}" +
//        "\nui = ${useIndex.getLocation()}," +
//        "\n$s"

    List parameterValues = ExtractParameters.extractParams(parameters)
//    println "Worker $collectMethodName $workerID has started with params $parameterValues"

    def inData
    RequestSend workRequest, indexRequest
    workRequest = new RequestSend(workerID)
    indexRequest = new RequestSend(workerID)
//    println "Worker $collectMethodName $workerID sending work request $workRequest"
    requestWork.write(workRequest)
//    println "Worker $collectMethodName $workerID has sent workRequest $workRequest"
    inData = inputWork.read()
//    println "Worker $collectMethodName $workerID has read $inData"
    while (!(inData instanceof TerminalIndex)){
      inData.&"$methodName"(parameterValues)
      requestIndex.write(indexRequest)
      RequestSend use = (useIndex.read() as RequestSend)
      outputWork[use.index].write(inData)
      requestWork.write(workRequest)
      inData = inputWork.read()
    }
    // a termination has been read, tell the node
//    println "Worker $collectMethodName $workerID has read termination"
    TerminalIndex terminalIndex = new TerminalIndex(workerID)
    workerToNode.write(terminalIndex)
//    println "Worker $workerID with $collectMethodName has terminated"
  }
}
