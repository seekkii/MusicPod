//
// Created by Umi on 10/14/2023.
//

#include <iostream>
#include <jni.h>
#include <thread>
#include <vector>
#include "avir/avir.h"
#include "avir/thread_pool.hpp"
#include <cmath>
#include <jni.h>


const int kernelSize =3;
void blurImageRegion(const uint8_t* inBufferAddress, uint8_t* sharpenedImage,
                     jint width, jint height,
                     int startRow, int endRow,
                     const int kernel[kernelSize][kernelSize], int radius) {

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
            sharpenedImage[position] = static_cast<uint8_t>(sumA); // Assuming alpha is 255 (fully opaque)
            sharpenedImage[position + 1] = static_cast<uint8_t>(sumR);
            sharpenedImage[position + 2] = static_cast<uint8_t>(sumG);
            sharpenedImage[position + 3] = static_cast<uint8_t>(sumB);
        }
    }
}


extern "C" JNIEXPORT void JNICALL
Java_com_example_myapplication_MainActivityKt_imgSharpenCPP(JNIEnv *env, jclass clazz,
                                                  jobject inBuf,
                                                  jobject outBuf,
                                                  jint width,
                                                  jint height
){
    const unsigned int numThreads = std::thread::hardware_concurrency();
    auto* inBufferAddress = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(inBuf));
    auto* outBufferAddress = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(outBuf));
    // Check if buffers and their addresses are valid
    if (outBufferAddress == nullptr ) {
        std::cout << "bfad null!" << std::endl;
        return;
    }
    const int laplacianKernel[3][3] = {
            { 0,  1,  0},
            { 1, -8,  1},
            { 0,  1,  0}
    };

    int radius = kernelSize / 2;
    std::vector<std::thread> threadPool;

    // Calculate the number of rows to process per thread
    int rowsPerThread = height / numThreads;
    int startRow = 0;

    // Launch threads to blur different regions of the image
    for (int i = 0; i < numThreads; i++) {
        int endRow = (i == numThreads - 1) ? height : startRow + rowsPerThread;
        threadPool.emplace_back(std::bind(blurImageRegion, inBufferAddress, outBufferAddress, width, height, startRow, endRow, laplacianKernel, radius));
        startRow = endRow;
    }

    // Unlock the mutex after all threads finish writing to outBufferAddress

    // Wait for all threads to finish
    for (auto& thread : threadPool) {
        thread.join();
    }
}




