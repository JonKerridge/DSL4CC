package BuildTests

import org.junit.Test

class VerifyTestObject1 {
    @Test
    public void test() {
        def et = new EmitTest1([10])
        def eo = et.create()
        int tv = 0
        while (eo.valid) {
            def to1 = eo.emittedObject
            to1.updateMethod(['500'])
            to1.collect()
            int testValue = tv+500
            assert to1.value == testValue:" test value mismatch ${to1.value} :: $testValue"
            tv++
            eo = et.create()
        }
    }
}
