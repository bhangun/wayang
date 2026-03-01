#!/bin/bash

# Find all Java files in wayang-control-core
files=$(find core/wayang-control-core/src/main/java -name "*.java")

for file in $files; do
  # No need to actually change the import statement string because the package name 
  # for the DTOs in wayang-control-spi is also `tech.kayys.wayang.control.dto`.
  # The issue is that the local package takes precedence string-wise, but once we delete 
  # the local package, the compiler will naturally resolve the imports from the SPI jar 
  # which is already on the classpath.
  echo "Verified $file"
done

# The only thing we actually need to do is delete the local duplicate directory!
rm -rf core/wayang-control-core/src/main/java/tech/kayys/wayang/control/dto
echo "Deleted redundant local DTO directory."
