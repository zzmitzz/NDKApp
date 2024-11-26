

// Optional: If you need to handle different YUV formats or do additional processing
void processImage(const jbyte* input, jbyte* output, jint width, jint height) {
    // YUV420 format already has grayscale in Y plane
    // Just copy the Y plane if no additional processing is needed
    memcpy(output, input, width * height);

    for (int i = 0; i < width * height; i++) {
        // Convert signed byte to unsigned
        int pixel = input[i] & 0xFF;

        // Apply contrast/brightness adjustments if needed
         pixel = (pixel * 5) + 15;

        // Clamp to byte range
        pixel = (pixel > 255) ? 255 : ((pixel < 0) ? 0 : pixel);

        output[i] = input[i];
    }
}