// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("myapplication");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("myapplication")
//      }
//    }

#include <iostream>
#include <jni.h>
#include <thread>
#include <vector>
#include "avir/avir.h"
#include "avir/thread_pool.hpp"
#include <cmath>
#include <jni.h>
#include "stackblur.h"



using thread_pool_base = nstd::thread_pool;
class avir_scale_thread_pool : public avir::CImageResizerThreadPool, public thread_pool_base
{
public:
    int getSuggestedWorkloadCount() const override
    {
        return thread_pool_base::size();
    }

    void addWorkload(CWorkload *const workload) override
    {
        _workloads.emplace_back(workload);
    }

    void startAllWorkloads() override
    {
        for (auto &workload : _workloads) _tasks.emplace_back(thread_pool_base::enqueue([](auto workload){ workload->process(); }, workload));
    }

    void waitAllWorkloadsToFinish() override
    {
        for (auto &task : _tasks) task.wait();
    }

    virtual void removeAllWorkloads()
    {
        _tasks.clear();
        _workloads.clear();
    }

private:
    std::deque<std::future<void>> _tasks;
    std::deque<CWorkload*> _workloads;
};


extern "C" JNIEXPORT void JNICALL
Java_com_example_myapplication_ThemeCustomizationKt_scaleWithAvir(JNIEnv *env, jclass clazz,
                                                            jobject inBuf,
                                                            jobject outBuf,
                                                            jint width, jint height,
                                                            jfloat scaleFactor,
                                                            jint bitResolution
){

    auto* inBufAddress = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(inBuf));
    auto* scaleImage = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(outBuf));
    avir_scale_thread_pool scaling_pool;
    avir::CImageResizerVars vars; vars.ThreadPool = &scaling_pool;
    avir::CImageResizerParamsUltra roptions;
    avir::CImageResizer<> image_resizer { bitResolution};
    image_resizer.resizeImage(inBufAddress, width, height, 0,
                              scaleImage,static_cast<int>(width * scaleFactor),
                              static_cast<int>(height * scaleFactor),
                              4, 0, &vars
                              );

}

extern "C" JNIEXPORT void JNICALL
Java_com_example_myapplication_ThemeCustomizationKt_imgTransparent(JNIEnv *env, jclass clazz,
                                                             jobject inBuf,
                                                             jlong capacity,
                                                             jfloat alpha) {
    // Get the direct buffer address
    auto* inBufferAddress = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(inBuf));

    // Ensure alpha is within the range [0, 1]
    alpha = std::max(0.0f, std::min(1.0f, alpha));

    // Process the image data (assuming 32-bit RGBA format)
    for (jlong pos = 0; pos < capacity; pos += 4) {
        uint8_t a = inBufferAddress[pos];
        uint8_t r = inBufferAddress[pos + 1];
        uint8_t g = inBufferAddress[pos + 2];
        uint8_t b = inBufferAddress[pos + 3];

        // Apply transparency by interpolating between original pixel value and transparent pixel value
        inBufferAddress[pos] = static_cast<uint8_t>(a * (1.0 - alpha)); // alpha channel
        inBufferAddress[pos + 1] = static_cast<uint8_t>(r * (1.0 - alpha));
        inBufferAddress[pos + 2] = static_cast<uint8_t>(g*(1.0 - alpha));
        inBufferAddress[pos + 3] = static_cast<uint8_t>(b*(1.0 - alpha));
    }
}

void generateSeperateGaussianKernel(float kernel[], float sigma, int kernelSize ) {
    const float pi = 3.14159265358979323846;
    float total = 0.0;

    int radius = kernelSize / 2; // Calculate the radius of the kernel

    for (int x = -radius; x <= radius; ++x) {
        float weight = exp(-(x * x ) / (2 * sigma * sigma)) / ( 2* pi * sigma * sigma);
        kernel[x + radius] = weight; // Shift the coordinates to fit the kernel
        total += weight;

    }

    // Normalize the kernel
    for (int x = 0; x < kernelSize; ++x) {
        kernel[x] /= total;

    }
}
void blurImageRowRegion(const uint8_t* inBufferAddress, uint8_t* blurredImage,
                        jint width, jint height,
                        int startRow, int endRow,
                        float kernel[], int kernelSize, int radius) {
    // Horizontal blur pass (along the rows)
    for (jint y = startRow; y < endRow; y++) {
        for (jint x = 0; x < width; x++) {
            float sumA = 0.0f;
            float sumR = 0.0f;
            float sumG = 0.0f;
            float sumB = 0.0f;

            for (int kx = -radius; kx <= radius; kx++) {
                int pixelX = x + kx;

                if (pixelX >= 0 && pixelX < width) {
                    // Calculate the position in the original image buffer
                    jlong position = (y * width + pixelX) * 4;

                    // Get the RGB values of the pixel
                    uint8_t a = inBufferAddress[position];
                    uint8_t r = inBufferAddress[position + 1];
                    uint8_t g = inBufferAddress[position + 2];
                    uint8_t b = inBufferAddress[position + 3];

                    // Accumulate the weighted values
                    sumA += kernel[kx + radius] * a;
                    sumR += kernel[kx + radius] * r;
                    sumG += kernel[kx + radius] * g;
                    sumB += kernel[kx + radius] * b;
                }
            }

            // Calculate the position in the blurred image buffer
            jlong position = (y * width + x) * 4;

            // Store the blurred pixel values in the temporary buffer
            blurredImage[position] = static_cast<uint8_t>(sumA); // Assuming alpha is 255 (fully opaque)
            blurredImage[position + 1] = static_cast<uint8_t>(sumR);
            blurredImage[position + 2] = static_cast<uint8_t>(sumG);
            blurredImage[position + 3] = static_cast<uint8_t>(sumB);
        }
    }
}


void blurImageColRegion(const uint8_t* inBufferAddress, uint8_t* blurredImage,
                     jint width, jint height,
                     int startRow, int endRow,
                     float kernel[], int kernelSize, int radius) {


    // Vertical blur pass (along the columns)
    for (jint y = startRow; y < endRow; y++) {
        for (jint x = 0; x < width; x++) {
            float sumA = 0.0f;
            float sumR = 0.0f;
            float sumG = 0.0f;
            float sumB = 0.0f;

            for (int ky = -radius; ky <= radius; ky++) {
                int pixelY = y + ky;

                if (pixelY >= 0 && pixelY < height) {
                    // Calculate the position in the blurred image buffer
                    jlong position = (pixelY * width + x) * 4;

                    // Accumulate the weighted values from the previously blurred pixels
                    sumA += kernel[ky + radius] * inBufferAddress[position];
                    sumR += kernel[ky + radius] * inBufferAddress[position + 1];
                    sumG += kernel[ky + radius] * inBufferAddress[position + 2];
                    sumB += kernel[ky + radius] * inBufferAddress[position + 3];
                }
            }

            // Calculate the position in the blurred image buffer
            jlong position = (y * width + x) * 4;

            // Store the blurred pixel values in the final buffer
            blurredImage[position] = static_cast<uint8_t>(sumA);
            blurredImage[position + 1] = static_cast<uint8_t>(sumR);
            blurredImage[position + 2] = static_cast<uint8_t>(sumG);
            blurredImage[position + 3] = static_cast<uint8_t>(sumB);
        }
    }
}

void blurringRow(const uint8_t* inBufferAddress, uint8_t* outBufferAddress, int width, int height, float kernel[], int kernelSize) {
    const unsigned int numThreads = std::thread::hardware_concurrency();
    int radius = kernelSize / 2;
    std::vector<std::thread> threadPool;
    // Calculate the number of rows to process per thread
    int rowsPerThread = height / numThreads;
    int startRow = 0;
    // Launch threads to blur different regions of the image
    for (int i = 0; i < numThreads; i++) {
        int endRow = (i == numThreads - 1) ? height : startRow + rowsPerThread;
        threadPool.emplace_back([=]() { blurImageRowRegion(inBufferAddress, outBufferAddress, width, height, startRow, endRow, kernel, kernelSize, radius); });
        startRow = endRow;
    }
    // Unlock the mutex after all threads finish writing to outBufferAddress
    // Wait for all threads to finish
    for (auto& thread : threadPool) {
        thread.join();
    }

}
void blurringCol(const uint8_t* inBufferAddress, uint8_t* outBufferAddress, int width, int height, float kernel[], int kernelSize) {
        const unsigned int numThreads = std::thread::hardware_concurrency();

        int radius = kernelSize / 2;
        std::vector<std::thread> threadPool;

        // Calculate the number of rows to process per thread
        int rowsPerThread = height / numThreads;
        int startRow = 0;

    for (int i = 0; i < numThreads; i++) {
        int endRow = (i == numThreads - 1) ? height : startRow + rowsPerThread;
        threadPool.emplace_back([inBufferAddress, outBufferAddress, width, height, startRow, endRow, kernel, kernelSize, radius] { return blurImageColRegion(inBufferAddress, outBufferAddress, width, height, startRow, endRow, kernel, kernelSize, radius); });
        startRow = endRow;
    }
        // Unlock the mutex after all threads finish writing to outBufferAddress
        // Wait for all threads to finish
        for (auto& thread : threadPool) {
            thread.join();
        }

    }

extern "C" JNIEXPORT void JNICALL
Java_com_example_myapplication_ThemeCustomizationKt_onedGaussBlur(JNIEnv *env, jclass clazz,
                                                            jobject inBuf,
                                                            jobject outBuf,
                                                            jint width,
                                                            jint height,
                                                            jint kernelNewSize,
                                                            jfloat sigma)
{
    auto* inBufferAddress = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(inBuf));
    auto* outBufferAddress = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(outBuf));

    float kernel[kernelNewSize]; // Use kernelNewSize instead of kernelSize
    generateSeperateGaussianKernel(kernel, sigma, kernelNewSize);

    blurringRow(inBufferAddress,outBufferAddress,width,height,kernel,kernelNewSize);
    blurringCol(outBufferAddress,outBufferAddress,width,height,kernel,kernelNewSize);
}


extern "C" JNIEXPORT void JNICALL
Java_com_example_myapplication_ThemeCustomizationKt_bilinearScale(JNIEnv* env, jclass clazz,
                                                            jobject originalByteBuffer,
                                                            jobject newByteBuffer,
                                                            jint width, jint height,
                                                            jint newWidth, jint newHeight) {
    // Get the direct buffer addresses
    auto* bufferAddress = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(originalByteBuffer));
    auto* newBufferAddress = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(newByteBuffer));

    // Check if buffers and their addresses are valid
    if (bufferAddress == nullptr) {
        throw std::runtime_error("originalByteBuffer is null!");
    }
    if (newBufferAddress == nullptr) {
        throw std::runtime_error("newByteBuffer is null!");
    }

    // Validate buffer sizes based on the expected ARGB format (4 channels per pixel)
    int expectedBufferSize = newWidth * newHeight * 4;
    int originalBufferSize = width * height * 4;
    if (env->GetDirectBufferCapacity(originalByteBuffer) < originalBufferSize ||
        env->GetDirectBufferCapacity(newByteBuffer) < expectedBufferSize) {
        throw std::runtime_error("Invalid buffer size!");
    }

    // Calculate scale factors for resizing
    float scaleX = static_cast<float>(width) / newWidth;
    float scaleY = static_cast<float>(height) / newHeight;

    // Function to perform bilinear resizing for a chunk of the new image
    auto resizeChunk = [&](int startY, int endY) {
        for (int newY = startY; newY < endY; newY++) {
            for (int newX = 0; newX < newWidth; newX++) {
                // Calculate the corresponding coordinates in the original image
                float origX = newX * scaleX;
                float origY = newY * scaleY;

                // Find the surrounding pixels' coordinates for bilinear interpolation
                int x0 = static_cast<int>(std::max(0.0f, std::min(origX, static_cast<float>(width - 1))));
                int x1 = static_cast<int>(std::max(0.0f, std::min(origX + 1, static_cast<float>(width - 1))));
                int y0 = static_cast<int>(std::max(0.0f, std::min(origY, static_cast<float>(height - 1))));
                int y1 = static_cast<int>(std::max(0.0f, std::min(origY + 1, static_cast<float>(height - 1))));

                // Calculate the interpolation weights
                float weightX = origX - x0;
                float weightY = origY - y0;

                // Calculate the position of the four surrounding pixels in the original image
                jlong position00 = (y0 * width + x0) * 4;
                jlong position10 = (y0 * width + x1) * 4;
                jlong position01 = (y1 * width + x0) * 4;
                jlong position11 = (y1 * width + x1) * 4;

                // Perform bilinear interpolation for each color channel (ARGB format)
                for (int channel = 0; channel < 4; channel++) {
                    uint8_t c00 = bufferAddress[position00 + channel];
                    uint8_t c10 = bufferAddress[position10 + channel];
                    uint8_t c01 = bufferAddress[position01 + channel];
                    uint8_t c11 = bufferAddress[position11 + channel];

                    // Calculate the new pixel value using bilinear interpolation
                    float interpolatedValue = (1 - weightX) * (1 - weightY) * c00 +
                                              weightX * (1 - weightY) * c10 +
                                              (1 - weightX) * weightY * c01 +
                                              weightX * weightY * c11;

                    // Store the new pixel value in the new buffer
                    newBufferAddress[(newY * newWidth + newX) * 4 + channel] = static_cast<uint8_t>(interpolatedValue);
                }
            }
        }
    };

    // Number of threads to use (adjust as needed)
    const unsigned int numThreads = std::thread::hardware_concurrency();

    // Check if hardware concurrency information is available
    if (numThreads == 0) {
        throw std::runtime_error("Cannot determine the number of threads supported.");
    }

    // Calculate the chunk size based on the number of threads and the new height
    int chunkSizeY = (newHeight + numThreads - 1) / numThreads;

    // Split the new image into chunks and process them using threads
    std::vector<std::thread> threads;
    for (int t = 0; t < numThreads; t++) {
        int startY = t * chunkSizeY;
        int endY = std::min((t + 1) * chunkSizeY, newHeight);
        threads.emplace_back(resizeChunk, startY, endY);
    }

    // Wait for all threads to finish
    for (auto& thread : threads) {
        thread.join();
    }
}





const int kernelSize =3;
void generateGaussianKernel( float kernel[kernelSize][kernelSize], float sigma) {
    const float pi = 3.14159265358979323846;
    float total = 0.0;

    int radius = kernelSize / 2; // Calculate the radius of the kernel

    for (int x = -radius; x <= radius; ++x) {
        for (int y = -radius; y <= radius; ++y) {
            float weight = exp(-(x * x + y * y) / (2 * sigma * sigma)) / (2 * pi * sigma * sigma);
            kernel[x + radius][y + radius] = weight; // Shift the coordinates to fit the kernel
            total += weight;
        }
    }

    // Normalize the kernel
    for (int x = 0; x < kernelSize; ++x) {
        for (int y = 0; y < kernelSize; ++y) {
            kernel[x][y] /= total;
        }
    }
}

void blurImageRegion(const uint8_t* inBufferAddress, uint8_t* blurredImage,
                     jint width, jint height,
                     int startRow, int endRow,
                     float kernel[kernelSize][kernelSize], int radius) {

    // Horizontal blur pass (along the rows) and vertical blur pass (along the columns)
    for (jint y = startRow; y < endRow; y++) {
        for (jint x = 0; x < width; x++) {
            float sumA = 0.0f;
            float sumR = 0.0f;
            float sumG = 0.0f;
            float sumB = 0.0f;

            for (int ky = -radius; ky <= radius; ky++) {
                int pixelY = y + ky;

                for (int kx = -radius; kx <= radius; kx++) {
                    int pixelX = x + kx;

                    if (pixelY >= 0 && pixelY < height && pixelX >= 0 && pixelX < width) {
                        // Calculate the position in the original image buffer
                        jlong position = (pixelY * width + pixelX) * 4;

                        // Get the RGB values of the pixel
                        uint8_t a = inBufferAddress[position];
                        uint8_t r = inBufferAddress[position + 1];
                        uint8_t g = inBufferAddress[position + 2];
                        uint8_t b = inBufferAddress[position + 3];

                        // Accumulate the weighted values
                        sumA += kernel[ky + radius][kx + radius] * a;
                        sumR += kernel[ky + radius][kx + radius] * r;
                        sumG += kernel[ky + radius][kx + radius] * g;
                        sumB += kernel[ky + radius][kx + radius] * b;
                    }
                }
            }
            // Calculate the position in the blurred image buffer
            jlong position = (y * width + x) * 4;

            // Store the final blurred pixel values in the output buffer
            blurredImage[position] = static_cast<uint8_t>(sumA); // Assuming alpha is 255 (fully opaque)
            blurredImage[position + 1] = static_cast<uint8_t>(sumR);
            blurredImage[position + 2] = static_cast<uint8_t>(sumG);
            blurredImage[position + 3] = static_cast<uint8_t>(sumB);
        }
    }
}


extern "C" JNIEXPORT void JNICALL
Java_com_example_myapplication_ThemeCustomizationKt_imgBlur(JNIEnv *env, jclass clazz,
                                                      jobject inBuf,
                                                      jobject outBuf,
                                                      jint width,
                                                      jint height,
                                                      jfloat sigma
){
    const unsigned int numThreads = std::thread::hardware_concurrency();
    auto* inBufferAddress = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(inBuf));
    auto* outBufferAddress = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(outBuf));
    // Check if buffers and their addresses are valid
    if (outBufferAddress == nullptr ) {
        std::cout << "bfad null!" << std::endl;
        return;
    }
    float kernel[kernelSize][kernelSize];
    generateGaussianKernel(kernel,sigma); // Adjust sigma value as needed

    int radius = kernelSize / 2;
    std::vector<std::thread> threadPool;

    // Calculate the number of rows to process per thread
    int rowsPerThread = height / numThreads;
    int startRow = 0;

    // Launch threads to blur different regions of the image
    for (int i = 0; i < numThreads; i++) {
        int endRow = (i == numThreads - 1) ? height : startRow + rowsPerThread;
        threadPool.emplace_back(std::bind(blurImageRegion, inBufferAddress, outBufferAddress, width, height, startRow, endRow, kernel, radius));
        startRow = endRow;
    }
    // Unlock the mutex after all threads finish writing to outBufferAddress

    // Wait for all threads to finish
    for (auto& thread : threadPool) {
        thread.join();
    }
}



//copy 2 buffers
extern "C" JNIEXPORT void JNICALL
Java_com_example_myapplication_ThemeCustomizationKt_bufferInCPP(
        JNIEnv *env, jclass clazz,
        jobject originalByteBuffer,
        jobject newByteBuffer,
        jint startX,
        jint startY,
        jint width,
        jint height,
        jint ogWidth,
        jint ogHeight){

    // Get the direct buffer addresses
    auto* bufferAddress = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(originalByteBuffer));
    auto* newBufferAddress = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(newByteBuffer));

    // Check if buffers and their addresses are valid
    if (bufferAddress == nullptr ) {
        std::cout << "bfad null!" << std::endl;
        return;
    }
    if (newBufferAddress == nullptr){
        std::cout << "new bf null!" << std::endl;
        return;
    }

    jint endX = startX + width;
    jint endY = startY + height;
    const unsigned int numThreads = std::thread::hardware_concurrency();
    std::vector<std::thread> threads;

    // Split the image into chunks and process them using threads
    int chunkSizeX = width / numThreads;
    for (int t = 0; t < numThreads; t++) {
        jint threadStartX = startX + t * chunkSizeX;
        jint threadEndX = (t == numThreads - 1) ? endX : startX + (t + 1) * chunkSizeX;
        threads.emplace_back([=] {
            // Process the chunk for this thread
            for (jint y = startY; y < endY; y++) {
                for (jint x = threadStartX; x < threadEndX; x++) {
                    jlong position = (y * ogWidth + x) * 4;
                    jlong newPosition = ((y - startY) * width + (x - startX)) * 4;

                    uint8_t a = bufferAddress[position];
                    uint8_t r = bufferAddress[position + 1];
                    uint8_t g = bufferAddress[position + 2];
                    uint8_t b = bufferAddress[position + 3];

                    newBufferAddress[newPosition] = a;
                    newBufferAddress[newPosition + 1] = r;
                    newBufferAddress[newPosition + 2] = g;
                    newBufferAddress[newPosition + 3] = b;
                }
            }
        });
    }

    // Wait for all threads to finish
    for (auto& thread : threads) {
        thread.join();
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_myapplication_ThemeCustomizationKt_copyRotateRegion(
        JNIEnv *env, jclass clazz,
        jobject originalByteBuffer,
        jobject newByteBuffer,
        jint startX,
        jint startY,
        jint width,
        jint height,
        jint ogWidth,
        jint ogHeight,
        jfloat alpha) { // Add rotation angle as a parameter

    // Get the direct buffer addresses
    auto* bufferAddress = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(originalByteBuffer));
    auto* newBufferAddress = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(newByteBuffer));

    // Check if buffers and their addresses are valid
    if (bufferAddress == nullptr ) {
        std::cout << "bfad null!" << std::endl;
        return;
    }
    if (newBufferAddress == nullptr){
        std::cout << "new bf null!" << std::endl;
        return;
    }
    // ... (existing code)

    jint endX = startX + width;
    jint endY = startY + height;
    const unsigned int numThreads = std::thread::hardware_concurrency();
    std::vector<std::thread> threads;

    // Split the image into chunks and process them using threads
    int chunkSizeX = width / numThreads;
    for (int t = 0; t < numThreads; t++) {
        jint threadStartX = startX + t * chunkSizeX;
        jint threadEndX = (t == numThreads - 1) ? endX : startX + (t + 1) * chunkSizeX;
        threads.emplace_back([=] {
            // Process the chunk for this thread
            for (jint y = startY; y < endY; y++) {
                for (jint x = threadStartX; x < threadEndX; x++) {
                    // Calculate rotated coordinates
                    float rotatedX = (x - startX) * cos(alpha) - (y - startY) * sin(alpha) + startX;
                    float rotatedY = (x - startX) * sin(alpha) + (y - startY) * cos(alpha) + startY;

                    // Interpolate neighboring pixel values (you may need more sophisticated interpolation)
                    jint x0 = static_cast<jint>(rotatedX);
                    jint y0 = static_cast<jint>(rotatedY);
                    jint x1 = std::min(x0 + 1, ogWidth - 1);
                    jint y1 = std::min(y0 + 1, ogHeight - 1);

                    // Bilinear interpolation
                    float xFraction = rotatedX - x0;
                    float yFraction = rotatedY - y0;

                    for (int channel = 0; channel < 4; channel++) {
                        float interpolatedValue = (1 - xFraction) * (1 - yFraction) * bufferAddress[(y0 * ogWidth + x0) * 4 + channel]
                                                  + xFraction * (1 - yFraction) * bufferAddress[(y0 * ogWidth + x1) * 4 + channel]
                                                  + (1 - xFraction) * yFraction * bufferAddress[(y1 * ogWidth + x0) * 4 + channel]
                                                  + xFraction * yFraction * bufferAddress[(y1 * ogWidth + x1) * 4 + channel];

                        newBufferAddress[((y - startY) * width + (x - startX)) * 4 + channel] = static_cast<uint8_t>(interpolatedValue);
                    }
                }
            }
        });
    }

    // ... (remaining code)
    // Wait for all threads to finish
    for (auto& thread : threads) {
        thread.join();
    }
}



extern "C" JNIEXPORT void JNICALL
Java_com_example_myapplication_ThemeCustomizationKt_stackBlur(
        JNIEnv *env, jclass clazz,
        jobject inBuf,
        jobject outBuf,
        jint width,
        jint height,
        jint rad
){
    const unsigned int numThreads = std::thread::hardware_concurrency();
    auto* inBufferAddress = reinterpret_cast<unsigned char *>(env->GetDirectBufferAddress(inBuf));
    auto* outBufferAddress = reinterpret_cast<unsigned char *>(env->GetDirectBufferAddress(outBuf));

    // Check for null pointers before proceeding
    if (inBufferAddress == nullptr || outBufferAddress == nullptr) {
        // Handle the error, for example, throw an exception or return early
        return;
    }

    // Use memcpy to copy the content of the buffer
    //std::memcpy(outBufferAddress, inBufferAddress, width * height * sizeof(unsigned char));

    // Now you can apply your stackblur function on the copied data
    stackblur(inBufferAddress,outBufferAddress, width, height, rad, numThreads);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_myapplication_ThemeCustomizationKt_memCopyInCpp(
        JNIEnv *env, jclass clazz,
        jobject inBuf,
        jobject outBuf,
        jlong capacity
) {
    const unsigned int numThreads = std::thread::hardware_concurrency();
    auto *inBufferAddress = reinterpret_cast<unsigned char *>(env->GetDirectBufferAddress(inBuf));
    auto *outBufferAddress = reinterpret_cast<unsigned char *>(env->GetDirectBufferAddress(outBuf));

    // Check for null pointers before proceeding
    if (inBufferAddress == nullptr || outBufferAddress == nullptr) {
        // Handle the error, for example, throw an exception or return early
        return;
    }

    // Use memcpy to copy the content of the buffer
    std::memcpy(outBufferAddress, inBufferAddress, capacity);

}