# Use the GraalVM base image for the build stage
FROM ghcr.io/graalvm/graalvm-community:24.0.1 AS build

# Install necessary dependencies and Node.js directly
ARG NODE_VERSION=20
RUN microdnf install -y gzip tar curl python3 gcc gcc-c++ make

# Install Node.js directly from NodeSource repository
RUN curl -fsSL https://rpm.nodesource.com/setup_${NODE_VERSION}.x | bash - && \
    microdnf install -y nodejs

# Verify Node.js installation
RUN node --version && npm --version

# Set the working directory inside the container
WORKDIR /project

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src/main src/main
# Build the native image using Maven in offline mode
RUN ./mvnw clean package -Pnative

# Copy Node.js native addon files
COPY binding.gyp .
COPY src/binding.cpp src/
COPY include/ include/
COPY index.* .
COPY package.json .
COPY package-lock.json .
COPY .npmignore .

# Run npm commands with the installed Node.js
RUN npm install
RUN npm run configure
RUN npm run build
RUN npm pack

CMD ["/bin/sh"]
