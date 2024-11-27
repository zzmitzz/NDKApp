#include <jni.h>
#include <string>
#include <ctime>
#include "process-image.h"
#include <android/log.h>

// String conversion
extern "C" JNIEXPORT jlong JNICALL
Java_com_example_ndkapp_MainActivity_calculateNthFibonacci(
        JNIEnv *env, jobject /* this */, jlong input) {
    jlong result = 0;
    jlong a = 0L;
    jlong b = 1L;
    std::time_t now = std::time(nullptr);
    for (jlong i = 0; i < input; i++) {
        result = a + b;
        a = b;
        b = result;
    }
    return std::time(nullptr) - now;
}


extern "C" JNIEXPORT jstring JNICALL
Java_com_example_ndkapp_MainActivity_readNameObject(
        JNIEnv *env,
        jobject /* this */,
        jobject personObject) {
    jclass userClass = env->GetObjectClass(personObject);

    jfieldID nameField = env->GetFieldID(userClass, "name", "Ljava/lang/String;");
    jfieldID ageField = env->GetFieldID(userClass, "age", "I");

    jstring jName = (jstring) env->GetObjectField(personObject, nameField);
//    const char* cName = env->GetStringUTFChars(jName, nullptr);
//    std::string name(cName);
//    env->ReleaseStringUTFChars(jName, cName);
//    env->DeleteLocalRef(jName);
    jint age = env->GetIntField(personObject, ageField);

    env->DeleteLocalRef(userClass);
    return jName;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_ndkapp_MainActivity_processImage(JNIEnv *env, jobject obj, jbyteArray yuv_data, jint width, jint height) {
    jsize len = env->GetArrayLength(yuv_data);
    jbyte *input = env->GetByteArrayElements(yuv_data, nullptr);

    // Create output array of same size
    jbyteArray output = env->NewByteArray(len);
    jbyte *outputPtr = env->GetByteArrayElements(output, nullptr);

    processImage(input,outputPtr,width,height);

    // Finalize
    env->SetByteArrayRegion(output, 0, len, outputPtr);
    env->ReleaseByteArrayElements(yuv_data, input, JNI_ABORT);
    env->ReleaseByteArrayElements(output, outputPtr, 0);

    return output;
}


//extern "C" {
//
//JNIEXPORT jbyteArray JNICALL
//Java_com_example_myapp_NativeLib_detectEdges(
//        JNIEnv *env,
//        jobject /* this */,
//        jbyteArray inputArray,
//        jint width,
//        jint height,
//        jdouble sigma,
//        jdouble threshold1,
//        jdouble threshold2
//) {
//    jbyte *inputBuffer = env->GetByteArrayElements(inputArray, nullptr);
//
//    // Convert byte array to Mat
//    cv::Mat inputImage(height, width, CV_8UC1, (unsigned char *)inputBuffer);
//    cv::Mat edges(height, width, CV_8UC1);
//
//    try {
//        // Apply Gaussian blur if sigma > 0
//        if (sigma > 0) {
//            cv::GaussianBlur(inputImage, inputImage, cv::Size(0, 0), sigma);
//        }
//
//        // Detect edges using Canny
//        cv::Canny(inputImage, edges, threshold1, threshold2);
//
//        // Create output byte array
//        jbyteArray resultArray = env->NewByteArray(width * height);
//        env->SetByteArrayRegion(resultArray, 0, width * height, (jbyte *)edges.data);
//
//        // Release resources
//        env->ReleaseByteArrayElements(inputArray, inputBuffer, 0);
//
//        return resultArray;
//    } catch (const cv::Exception& e) {
//        __android_log_print(ANDROID_LOG_ERROR, "EdgeDetection", "OpenCV error: %s", e.what());
//        env->ReleaseByteArrayElements(inputArray, inputBuffer, 0);
//        return nullptr;
//    }
//}
//
//} // extern "C"