package dsl4cc.DSLrecords

class EmittedObject <T> {
  boolean valid
  T emittedObject

  String toString() {
    return "EO: $valid = $emittedObject"
  }
}
