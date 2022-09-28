package dsl4cc.DSLparse

import dsl4cc.DSLrecords.ParseRecord
import groovyjarjarpicocli.CommandLine

class DSLParser {

    String inputFileName, outputTextFile, outObjectFile

    DSLParser(String inFileName) {
        this.inputFileName = inFileName
        outputTextFile = inputFileName + "txt"
        outObjectFile = inputFileName + "struct"
    }

    String hostIPAddress

    class HostSpecification {
        @CommandLine.Option( names = "-ip", description = "the IP address of the host")
        String hostIP
    } // HostSpecification

    class EmitSpecification {
        @CommandLine.Option(names = ["-n", "-nodes"], description = "number of nodes") int nodes
        @CommandLine.Option(names = ["-w", "-workers"], description = "number of workers per node") int workers
        @CommandLine.Option(names = ["-c", "-class"], description = "name of class implementing the EmitInterface")
        String className
        @CommandLine.Parameters ( description =" IP address for each node") List <String> nodeIPs
        // can be placed anywhere in specification but the number of specified IPs must match the number of nodes
        @CommandLine.Option( names = "-p", split = "!") List paramStrings
        // comma separated type, value pairs with no spaces, each string separated by ! and with NO spaces
        // the number of parameter strings MUST match the value of nodes * workers
    } // EmitSpecification

    class WorkSpecification {
        @CommandLine.Option(names = ["-n", "-nodes"], description = "number of nodes")
        int nodes
        @CommandLine.Option(names = ["-w", "-workers"], description = "number of workers per node")
        int workers
        @CommandLine.Option(names = ["-m", "-method"], description = "name of method used in this cluster")
        String methodName
        @CommandLine.Parameters ( description =" IP address for each node") List <String> nodeIPs
        // can be placed anywhere in specification but the number of specified IPs must match the number of nodes
        @CommandLine.Option( names = "-p") String paramString
        // comma separated type, value pairs with no spaces or other punctuation
        // three phase worker additions
        @CommandLine.Option(names = "-3p", description = "flag to indicate three phase worker")
        boolean threePhase
    } // WorkSpecification

    class CollectSpecification {
        @CommandLine.Option(names = ["-n", "-nodes"], description = "number of nodes")
        int nodes
        @CommandLine.Option(names = ["-w", "-workers"], description = "number of workers per node")
        int workers
        @CommandLine.Option(names = ["-cm", "-cmethod"], description = "name of collect method used in this cluster")
        String collectMethod
        @CommandLine.Option( names = ["-f", "-file"]) String outFileName  // base name of object file to which collected records are written
        // comma separated type, value pairs with no spaces or other punctuation
        @CommandLine.Option( names = "-cp", split = "!") List collectParamStrings  // parameters of the collect method, one per collecter process
        // comma separated type, value pairs with no spaces or other punctuation
        @CommandLine.Option(names = ["-fm", "-fmethod"], description = "name of finalise method used in this cluster")
        String finaliseMethod
        @CommandLine.Option( names = "-fp", split = "!") List finaliseParamStrings  // parameters of the finalise method, one per collecter process
        // comma separated type, value pairs with no spaces or other punctuation
        @CommandLine.Parameters ( description =" IP address for each node") List <String> nodeIPs
        // can be placed anywhere in specification but the number of specified IPs must match the number of nodes
    } // CollectSpecification

    boolean checkIPUniqueness (List<ParseRecord> buildData){
        List <String> usedIPs = []
        buildData.each {record ->
            if ( record.fixedIPAddresses != null)
                usedIPs = usedIPs + record.fixedIPAddresses
        }
        int totalSize = usedIPs.size()
        if (totalSize != usedIPs.toUnique().size()){
            println "The specified IPs are not unique $usedIPs"
            return false
        }
        else
            return true
    } // checkIPUniqueness

    boolean parse(){
        List<ParseRecord> buildData
        buildData = []
        new File(inputFileName).eachLine{ String inLine ->
            List<String> tokens = inLine.tokenize()
            String lineType = tokens.pop()
            String[] args = tokens.toArray(new String[0])
            ParseRecord parseRecord = new ParseRecord()
            switch (lineType) {
                case 'host':
                    HostSpecification host = new HostSpecification()
                    new CommandLine(host).parseArgs(args)
                    println "HostIP = ${host.hostIP}"
                    parseRecord.typeName = lineType
                    parseRecord.hostAddress = host.hostIP
                    hostIPAddress = host.hostIP
                    buildData << parseRecord
                    break
                case 'emit':
                    EmitSpecification emit = new EmitSpecification()
                    new CommandLine(emit).parseArgs(args)
                    int totalParamString = emit.nodes * emit.workers
                    println "Emit: Nodes = ${emit.nodes}, Workers = ${emit.workers}, Class = ${emit.className}, IPs = ${emit.nodeIPs}, Params = ${emit.paramStrings}"
                    // assumes emitters always have a parameter string associated with them
                    assert (emit.paramStrings.size() == totalParamString): "Emit must have $totalParamString parameter strings ${emit.paramStrings} supplied "
                    if (emit.nodeIPs != null)
                        assert emit.nodes == emit.nodeIPs.size(): "Emit: Number of specified IPs must be same as number of nodes"
                    parseRecord.typeName = lineType
                    parseRecord.hostAddress = hostIPAddress
                    parseRecord.nodes = emit.nodes
                    parseRecord.workers = emit.workers
                    parseRecord.classNameString = emit.className
                    if (emit.nodeIPs != null)
                        emit.nodeIPs.each { parseRecord.fixedIPAddresses << it }
                    // deal with the mandatory parameter string associated with each emitter
                    emit.paramStrings.each { String paramSpec ->
                        List<String> tokenizedParams
                        tokenizedParams = paramSpec.tokenize(',')
                        parseRecord.emitParameterString << tokenizedParams
                    }
                    buildData << parseRecord
                    break
                case 'work':
                    WorkSpecification work = new WorkSpecification()
                    new CommandLine(work).parseArgs(args)
                    println "Work: Nodes = ${work.nodes}, Workers = ${work.workers}, Method = ${work.methodName}, IPs = ${work.nodeIPs}, Params = ${work.paramString}"
                    if (work.nodeIPs != null)
                        assert work.nodes == work.nodeIPs.size(): "Work: Number of specified IPs must be same as number of nodes"
                    parseRecord.typeName = lineType
                    parseRecord.hostAddress = hostIPAddress
                    parseRecord.nodes = work.nodes
                    parseRecord.workers = work.workers
                    if (work.nodeIPs != null)
                        work.nodeIPs.each { parseRecord.fixedIPAddresses << it }
                    parseRecord.methodNameString = work.methodName
                    if (work.paramString != null)
                        parseRecord.parameterString = work.paramString.tokenize(',')
                    buildData << parseRecord
                    break
                case 'collect':
                    CollectSpecification collect = new CollectSpecification()
                    new CommandLine(collect).parseArgs(args)
                    println "Collect: Nodes = ${collect.nodes}, Workers = ${collect.workers}, OutFile = ${collect.outFileName}, " +
                            "CMethod = ${collect.collectMethod}, CParams = ${collect.collectParamStrings}, " +
                            "FMethod = ${collect.finaliseMethod}, FParams = ${collect.finaliseParamStrings}, IPs = ${collect.nodeIPs}"
                    if (collect.nodeIPs != null)
                        assert collect.nodes == collect.nodeIPs.size(): "Collect: Number of specified IPs must be same as number of nodes"
                    int totalParamString = collect.nodes * collect.workers
                    if (collect.collectParamStrings != null)
                        assert (collect.collectParamStrings.size() == totalParamString): "Collect must have $totalParamString parameter strings ${collect.collectParamStrings} supplied "
                    if (collect.finaliseParamStrings != null)
                        assert (collect.finaliseParamStrings.size() == totalParamString): "Collect must have $totalParamString parameter strings ${collect.finaliseParamStrings} supplied "
                    parseRecord.typeName = lineType
                    parseRecord.hostAddress = hostIPAddress
                    parseRecord.nodes = collect.nodes
                    parseRecord.workers = collect.workers
                    parseRecord.methodNameString = collect.collectMethod
                    parseRecord.outFileName = collect.outFileName
                    if (collect.nodeIPs != null)
                        collect.nodeIPs.each { parseRecord.fixedIPAddresses << it }
                    if (collect.collectParamStrings != null)
                        collect.collectParamStrings.each { String paramSpec ->
                            List<String> tokenizedParams
                            tokenizedParams = paramSpec.tokenize(',')
                            parseRecord.collectParameterString << tokenizedParams
                        }
                    parseRecord.finaliseNameString = collect.finaliseMethod
                    if (collect.finaliseParamStrings != null)
                        collect.finaliseParamStrings.each { String paramSpec ->
                            List<String> tokenizedParams
                            tokenizedParams = paramSpec.tokenize(',')
                            parseRecord.finaliseParameterString << tokenizedParams
                        }
                    buildData << parseRecord
                    break
                default:
                    println "$lineType incorrectly specified"
            }
        }  // file each line
        // check all specified IP addresses for nodes are unique
        if (checkIPUniqueness(buildData)) {
            File outFile = new File(outputTextFile)
            PrintWriter printWriter = outFile.newPrintWriter()
            buildData.each { printWriter.println "$it" }
            printWriter.flush()
            printWriter.close()
            File outObjFile = new File(outObjectFile)
            ObjectOutputStream outStream = outObjFile.newObjectOutputStream()
            buildData.each { outStream << it }
            outStream.flush()
            outStream.close()
            println "Parsing completed - no errors in $inputFileName"
            return true
        } else {
            println "Parsing failed, see errors highlighted above in $inputFileName"
            return false
        }
    }// parse

}
