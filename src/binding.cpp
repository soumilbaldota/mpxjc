#include <napi.h>
#include <string>
#include <vector>
#include "libnativeprojectreader.h"

Napi::Value ReadProjectSync(const Napi::CallbackInfo& info) {
    Napi::Env env = info.Env();

    if (info.Length() < 1 || !info[0].IsString()) {
        Napi::TypeError::New(env, "Expected a file path string").ThrowAsJavaScriptException();
        return env.Null();
    }

    std::string filePath = info[0].As<Napi::String>();
    graal_isolate_t* isolate = nullptr;
    graal_isolatethread_t* thread = nullptr;

    if (graal_create_isolate(nullptr, &isolate, &thread) != 0) {
        Napi::Error::New(env, "Failed to initialize GraalVM isolate").ThrowAsJavaScriptException();
        return env.Null();
    }

    int len = readProjectAndStore(thread, const_cast<char*>(filePath.c_str()));
    if (len <= 0) {
        graal_tear_down_isolate(thread);
        Napi::Error::New(env, "readProject failed").ThrowAsJavaScriptException();
        return env.Null();
    }

    std::vector<uint8_t> buffer(len);
    int copied = copyStoredProjectBytes(thread, reinterpret_cast<char*>(buffer.data()), len);

    graal_tear_down_isolate(thread);

    if (copied != len) {
        Napi::Error::New(env, "Byte copy mismatch").ThrowAsJavaScriptException();
        return env.Null();
    }

    return Napi::Buffer<uint8_t>::Copy(env, buffer.data(), buffer.size());
}

class GraalReaderWorker : public Napi::AsyncWorker {
public:
    GraalReaderWorker(Napi::Function& callback, const std::string& path)
        : Napi::AsyncWorker(callback), filePath_(path) {}

    void Execute() override {
        graal_isolate_t* isolate = nullptr;
        graal_isolatethread_t* thread = nullptr;

        if (graal_create_isolate(nullptr, &isolate, &thread) != 0) {
            SetError("Failed to initialize GraalVM isolate");
            return;
        }

        storedLength_ = readProjectAndStore(thread, const_cast<char*>(filePath_.c_str()));
        if (storedLength_ <= 0) {
            graal_tear_down_isolate(thread);
            SetError("readProject failed");
            return;
        }

        result_.resize(storedLength_);
        int copied = copyStoredProjectBytes(thread, reinterpret_cast<char*>(result_.data()), storedLength_);

        graal_tear_down_isolate(thread);

        if (copied != storedLength_) {
            SetError("Byte copy mismatch or failed");
        }
    }

    void OnOK() override {
        Napi::HandleScope scope(Env());
        Callback().Call({
            Env().Null(),
            Napi::Buffer<uint8_t>::Copy(Env(), result_.data(), result_.size())
        });
    }

    void OnError(const Napi::Error& e) override {
        Napi::HandleScope scope(Env());
        Callback().Call({ e.Value(), Env().Undefined() });
    }

private:
    std::string filePath_;
    int storedLength_ = 0;
    std::vector<uint8_t> result_;
};

Napi::Value ReadProjectAsync(const Napi::CallbackInfo& info) {
    Napi::Env env = info.Env();

    if (info.Length() < 2 || !info[0].IsString() || !info[1].IsFunction()) {
        Napi::TypeError::New(env, "Expected (string path, function callback)").ThrowAsJavaScriptException();
        return env.Null();
    }

    std::string filePath = info[0].As<Napi::String>();
    Napi::Function callback = info[1].As<Napi::Function>();

    auto* worker = new GraalReaderWorker(callback, filePath);
    worker->Queue();
    return env.Undefined();
}

Napi::Object Init(Napi::Env env, Napi::Object exports) {
    exports.Set("readProjectSync", Napi::Function::New(env, ReadProjectSync));
    exports.Set("readProjectAsync", Napi::Function::New(env, ReadProjectAsync));
    return exports;
}

NODE_API_MODULE(graal_reader, Init)