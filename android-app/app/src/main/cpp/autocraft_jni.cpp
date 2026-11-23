#include <jni.h>

namespace {
constexpr char kStatus[] =
    "Native bridge loaded. Blender subsystems are not linked yet.";
}

extern "C" JNIEXPORT jstring JNICALL
Java_ai_autocraft3d_MainActivity_nativeStatus(JNIEnv* env, jobject /* thiz */) {
  return env->NewStringUTF(kStatus);
}
