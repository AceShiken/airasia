#!/bin/bash

# Script to run the Currency Conversion API locally

echo "======================================"
echo "Currency Conversion API - Run Script"
echo "======================================"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version: $(java -version 2>&1 | head -n 1)"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi

echo "✅ Maven version: $(mvn -version | head -n 1)"
echo ""

# Check if API key is configured
if grep -q "YOUR_API_KEY_HERE" src/main/resources/application.yml; then
    echo "⚠️  Warning: API key not configured in application.yml"
    echo "Please update the API key in src/main/resources/application.yml"
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo "🔨 Building the project..."
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "🚀 Starting the application..."
    echo "API will be available at: http://localhost:8080"
    echo ""
    echo "Press Ctrl+C to stop the application"
    echo ""
    mvn spring-boot:run
else
    echo ""
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi
