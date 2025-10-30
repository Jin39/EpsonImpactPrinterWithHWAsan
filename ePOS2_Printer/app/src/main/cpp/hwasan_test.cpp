#include <jni.h>
#include <string>
#include <android/log.h>
#include <stdlib.h>
#include <cstring>

#define LOG_TAG "HWASanTest"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

// Test 1: Buffer Overflow - Should be detected by HWASan
JNIEXPORT jstring JNICALL
Java_com_epson_epos2_1printer_HWASanTestHelper_testBufferOverflow(JNIEnv *env, jobject /* this */) {
    LOGD("Starting buffer overflow test...");
    
    char *buffer = new char[10];
    strcpy(buffer, "Hello");
    
    // Intentional buffer overflow - HWASan should detect this
    buffer[15] = 'X';  // Writing beyond buffer bounds
    
    LOGD("Buffer overflow test completed");
    return env->NewStringUTF("Buffer overflow test executed");
}

// Test 2: Use After Free - Should be detected by HWASan
JNIEXPORT jstring JNICALL
Java_com_epson_epos2_1printer_HWASanTestHelper_testUseAfterFree(JNIEnv *env, jobject /* this */) {
    LOGD("Starting use-after-free test...");
    
    char* ptr = (char*)malloc(100);
    strcpy(ptr, "Hello World");
    
    free(ptr);
    
    // Intentional use-after-free - HWASan should detect this
    ptr[0] = 'X';  // Accessing freed memory
    
    LOGD("Use-after-free test completed");
    return env->NewStringUTF("Use-after-free test executed");
}

// Test 3: Double Free - Should be detected by HWASan
JNIEXPORT jstring JNICALL
Java_com_epson_epos2_1printer_HWASanTestHelper_testDoubleFree(JNIEnv *env, jobject /* this */) {
    LOGD("Starting double-free test...");
    
    char* ptr = (char*)malloc(50);
    strcpy(ptr, "Test");
    
    free(ptr);
    // Intentional double-free - HWASan should detect this
    free(ptr);  // Freeing already freed memory
    
    LOGD("Double-free test completed");
    return env->NewStringUTF("Double-free test executed");
}

// Test 4: Safe operation - Should work fine
JNIEXPORT jstring JNICALL
Java_com_epson_epos2_1printer_HWASanTestHelper_testSafeOperation(JNIEnv *env, jobject /* this */) {
    LOGD("Starting safe operation test...");
    
    char buffer[100];
    strcpy(buffer, "This is a safe operation");
    
    char* ptr = (char*)malloc(50);
    strcpy(ptr, "Safe malloc");
    free(ptr);
    
    LOGD("Safe operation test completed");
    return env->NewStringUTF("Safe operation completed successfully");
}

// Get HWASan status
JNIEXPORT jstring JNICALL
Java_com_epson_epos2_1printer_HWASanTestHelper_getHWASanStatus(JNIEnv *env, jobject /* this */) {
    LOGD("Checking HWASan status...");
    
#ifdef __has_feature
    #if __has_feature(hwaddress_sanitizer)
        LOGD("HWASan is enabled");
        return env->NewStringUTF("HWASan is ENABLED");
    #else
        LOGD("HWASan is disabled");
        return env->NewStringUTF("HWASan is DISABLED");
    #endif
#else
    LOGD("HWASan feature check unavailable");
    return env->NewStringUTF("HWASan status unknown");
#endif
}

}