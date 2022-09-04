package DSLrecords

class EmittedObject <T> {
  boolean valid
  T emittedObject

  String toString() {
    return "EO: $valid = $emittedObject"
  }
}
