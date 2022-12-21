package dsl4cc.DSLprocesses

import dsl4cc.DSLrecords.Acknowledgement
import dsl4cc.DSLrecords.ExtractParameters
import dsl4cc.DSLrecords.RequestSend
import dsl4cc.DSLrecords.TerminalIndex
import groovy_jcsp.ChannelOutputList
import jcsp.lang.CSProcess
import jcsp.lang.ChannelOutput
import jcsp.net2.Any2NetChannel
import jcsp.net2.NetAltingChannelInput
import jcsp.net2.NetChannelOutput
import jcsp.net2.NetSharedChannelInput
import jcsp.net2.NetSharedChannelOutput

class DSL4CC_Worker implements CSProcess{

  // net channel connections to and from Host
  NetSharedChannelOutput toHost     // writes to Host: fromNodes
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
    long startTime, elapsed
    List parameterValues = ExtractParameters.extractParams(parameters)
//    println "Worker $collectMethodName $workerID has started with params $parameterValues"

    Acknowledgement ack
    ack = new Acknowledgement(6, "Worker-$workerID")
    toHost.write(ack )
    startTime = System.currentTimeMillis()
    ack = fromHost.read() as Acknowledgement
    assert ack.ackValue == 6 :"Worker-$workerID expected ack = 6 got ${ack.ackValue}"
//    println "Worker $workerID running "
    println "Worker: $workerID, method: $methodName, parameters: $parameters, running "

    def inData
    RequestSend workRequest, indexRequest
    workRequest = new RequestSend(workerID)
    indexRequest = new RequestSend(workerID)
//    println "Worker $methodName $workerID sending work request $workRequest"
    requestWork.write(workRequest)
//    println "Worker $methodName $workerID has sent workRequest $workRequest"
    inData = inputWork.read()
    while (!(inData instanceof TerminalIndex)){
//      println "Worker $methodName $workerID has read $inData"
      if (parameterValues == null)
        inData.&"$methodName"()
      else
        inData.&"$methodName"(parameterValues)
      requestIndex.write(indexRequest)
      RequestSend use = (useIndex.read() as RequestSend)
      outputWork[use.index].write(inData)
      requestWork.write(workRequest)
      inData = inputWork.read()
    }
    // a termination has been read, tell the node
    elapsed = System.currentTimeMillis() - startTime
    TerminalIndex terminalIndex = new TerminalIndex("Worker", workerID, elapsed)
    workerToNode.write(terminalIndex)
//    println "Worker $workerID with $methodName has terminated after $elapsed millisecs"
  }
}
