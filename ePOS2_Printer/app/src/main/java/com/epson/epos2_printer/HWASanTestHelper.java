package com.epson.epos2_printer;

public class HWASanTestHelper {
    
    // Load the native library
    static {
        System.loadLibrary("hwasantest");
    }
    
    // Native method declarations
    public native String testBufferOverflow();
    public native String testUseAfterFree();
    public native String testDoubleFree();
    public native String testSafeOperation();
    public native String getHWASanStatus();
    
    // Helper method to run all tests
    public String runAllTests() {
        StringBuilder result = new StringBuilder();
        result.append("=== HWASan Test Results ===\n\n");
        
        try {
            result.append("HWASan Status: ").append(getHWASanStatus()).append("\n\n");
            
            result.append("1. Safe Operation Test:\n");
            result.append(testSafeOperation()).append("\n\n");
            
            result.append("2. Buffer Overflow Test:\n");
            result.append(testBufferOverflow()).append("\n\n");
            
            result.append("3. Use After Free Test:\n");
            result.append(testUseAfterFree()).append("\n\n");
            
            result.append("4. Double Free Test:\n");
            result.append(testDoubleFree()).append("\n\n");
            
        } catch (Exception e) {
            result.append("Error during testing: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }
}