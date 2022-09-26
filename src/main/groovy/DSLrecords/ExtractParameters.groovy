package DSLrecords

class ExtractParameters {
  static def extractParams = { List pList ->
//      println "params to be processed = $pList"
    List params = []
    int pointer
    pointer = 0
    int pSize = pList.size()   // each param spec comprises type-specification value
    while( pointer < pSize ){
      String pType = pList[pointer]
      pointer++
      String pString = pList[pointer]
      pointer++
//        println "param tokens = $pType :: $pString"
      switch (pType){
        case 'int':
          params << Integer.parseInt(pString)
          break
        case 'float':
          params << Float.parseFloat(pString)
          break
        case 'String':
          params << pString
          break
        case 'double':
          params << Double.parseDouble(pString)
          break
        case 'long':
          params << Long.parseLong(pString)
          break
        case 'boolean':
          params << Boolean.parseBoolean(pString)
          break
        default:
          println "Processing parameter string unexpectedly found type = $pType, value = $pString]"
          break
      } // end switch
    } // while
//      println "returned params = $params"
    return params
  } // extract params

}
