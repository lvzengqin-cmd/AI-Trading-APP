# Android Build Environment for Shijian AI Trading App
FROM openjdk:17-slim

# Install required packages
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    git \
    && rm -rf /var/lib/apt/lists/*

# Set environment variables
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools

# Download and install Android SDK command line tools
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O /tmp/cmdline-tools.zip \
    && unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools \
    && mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && rm /tmp/cmdline-tools.zip

# Accept licenses and install SDK components
RUN yes | sdkmanager --licenses \
    && sdkmanager "platforms;android-34" \
    && sdkmanager "build-tools;34.0.0" \
    && sdkmanager "platform-tools"

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Make gradlew executable
RUN chmod +x gradlew

# Build the APK
RUN ./gradlew assembleRelease --no-daemon

# Output directory
RUN mkdir -p /output \
    && cp app/build/outputs/apk/release/*.apk /output/ 2>/dev/null || cp app/build/outputs/apk/debug/*.apk /output/

CMD ["ls", "-la", "/output/"]
