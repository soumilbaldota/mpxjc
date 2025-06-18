{
  "targets": [
    {
      "target_name": "mpxj_node",
      "sources": [
        "src/binding.cpp"
      ],
      "include_dirs": [
        "<!@(node -p \"require('node-addon-api').include\")",
        "<(module_root_dir)/include",
        "include",
      ],
      "libraries": [],
      "dependencies": [
        "<!(node -p \"require('node-addon-api').gyp\")"
      ],
      "cflags!": ["-fno-exceptions"],
      "cflags_cc!": ["-fno-exceptions"],
      "defines": [
        "NAPI_DISABLE_CPP_EXCEPTIONS"
      ],
      "conditions": [
        ["OS=='win'", {
          "libraries": [
            "<(module_root_dir)/include/libnativeprojectreader.lib"
          ],
          "copies": [
            {
              "destination": "<(module_root_dir)/build/Release/",
              "files": [
                "<(module_root_dir)/include/libnativeprojectreader.dll"
              ]
            }
          ],
          "msvs_settings": {
            "VCCLCompilerTool": {
              "ExceptionHandling": 1
            }
          }
        }],
        ["OS=='linux'", {
          "libraries": [
            "-L<(module_root_dir)/include",
            "-lnativeprojectreader"
          ],
          "copies": [
            {
              "destination": "<(module_root_dir)/build/Release/",
              "files": [
                "<!@(find <(module_root_dir)/include -name '*.so' -o -name '*.so.*' 2>/dev/null || echo '')"
              ]
            }
          ],
          "ldflags": [
            "-Wl,-rpath,'$$ORIGIN'",
            "-Wl,-rpath,'$$ORIGIN/../include'"
          ]
        }]
      ]
    }
  ]
}