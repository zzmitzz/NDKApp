

#include <jni.h>
#include <android/log.h>
#define LOG_TAG "MyNativeCode"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)



void processImage(const jbyte* input, jbyte* output, jint width, jint height) {
    const int bytesPerPixel = 3;
    const int totalPixels = width * height;
    const int totalBytes = totalPixels * bytesPerPixel;

    for (int i = 0; i < totalPixels; i++) {
        int inputIndex = i * bytesPerPixel;

        // Read as unsigned bytes
        uint8_t r = static_cast<uint8_t>(input[inputIndex] & 0xFF);
        uint8_t g = static_cast<uint8_t>(input[inputIndex + 1] & 0xFF);
        uint8_t b = static_cast<uint8_t>(input[inputIndex + 2] & 0xFF);

        // Convert to grayscale
        int gray = (int)(0.299f * r + 0.587f * g + 0.114f * b);

//        // Apply contrast/brightness adjustments
//        gray = (gray * 5) - 93;

        // Clamp values
        gray = std::min(255, std::max(0, gray));
        // Ensure we're writing valid unsigned bytes
        output[inputIndex] = static_cast<jbyte>(gray);
        output[inputIndex + 1] = static_cast<jbyte>(gray);
        output[inputIndex + 2] = static_cast<jbyte>(gray);
    }
}