//
// Created by anhnt1061 on 20/11/2024.
//

#ifndef NDKAPP_USERCLASS_H
#define NDKAPP_USERCLASS_H


#include <jni.h>

using namespace std;
class UserClass {
private:
    JNIEnv *env;
    jobject userObject;
    jclass userClass;

    // Cache field IDs => Always cache in a local variable
    jfieldID nameField;
    jfieldID ageField;
public:
    explicit UserClass(JNIEnv *env) : env(env) {
        // Get User class
        userClass = env->FindClass("com/example/ndkapp/Person");

        // Cache all field IDs
        nameField = env->GetFieldID(userClass, "name", "Ljava/lang/String;");
        ageField = env->GetFieldID(userClass, "age", "I");
        // Create new User object
        userObject = env->AllocObject(userClass);
    }

    void setName(const char *name) {
        env->SetObjectField(userObject, nameField, env->NewStringUTF(name));
    }

    void setAge(int age) {
        env->SetIntField(userObject, ageField, age);
    }

    jobject getUserObject() {
        return userObject;
    }

    jint getAge() {
        return env->GetIntField(userObject, ageField);
    }

    jstring getName() {
        jstring jName = (jstring) env->GetObjectField(userObject, nameField);
        const char *cName = env->GetStringUTFChars(jName, nullptr);
        env->ReleaseStringUTFChars(jName, cName);
        env->DeleteLocalRef(jName);
        return jName;
    }

    ~UserClass() {
        // Clean up any remaining local references
        env->DeleteLocalRef(userClass);
    }
};


#endif //NDKAPP_USERCLASS_H
