#!/bin/bash
# ============================================
# JakeBooks E2E Test Setup & Execution Script
# ============================================
# Purpose: Automate E2E test setup and execution
# Usage: chmod +x run-e2e-tests.sh && ./run-e2e-tests.sh

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
JAKEBOOKS_DIR="$(cd "$(dirname "$0")" && pwd)"
DB_NAME="jakebooks"
DB_USER="${DB_USER:-postgres}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
APP_PORT="${APP_PORT:-8080}"
APP_URL="http://localhost:$APP_PORT"
TIMEOUT=30

echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   JakeBooks E2E Test Setup & Runner    ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
echo ""

# Function to print step
print_step() {
    echo -e "${GREEN}[STEP]${NC} $1"
}

# Function to print error
print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to print warning
print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# Function to print info
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# ============================================
# Step 1: Check Prerequisites
# ============================================
print_step "Checking prerequisites..."

# Check Java
if ! command -v java &> /dev/null; then
    print_error "Java is not installed. Please install Java 21+"
    exit 1
fi
JAVA_VERSION=$(java -version 2>&1 | head -n 1)
print_info "Found: $JAVA_VERSION"

# Check Maven
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven 3.8+"
    exit 1
fi
MVN_VERSION=$(mvn -version | head -n 1)
print_info "Found: $MVN_VERSION"

# Check PostgreSQL
if ! command -v psql &> /dev/null; then
    print_warning "psql not in PATH, skipping database check"
else
    print_info "PostgreSQL client found"
fi

# Check WebDriver
if command -v chromedriver &> /dev/null; then
    print_info "ChromeDriver found"
elif command -v geckodriver &> /dev/null; then
    print_info "GeckoDriver (Firefox) found"
else
    print_warning "No WebDriver found - tests will attempt to download automatically"
fi

echo ""

# ============================================
# Step 2: Verify Database
# ============================================
print_step "Verifying PostgreSQL database..."

if command -v psql &> /dev/null; then
    if psql -U "$DB_USER" -h "$DB_HOST" -d "$DB_NAME" -c "SELECT 1" &>/dev/null; then
        print_info "Database '$DB_NAME' is accessible"
    else
        print_warning "Database '$DB_NAME' not found or not accessible"
        print_info "Attempting to create database..."
        createdb -U "$DB_USER" -h "$DB_HOST" "$DB_NAME" 2>/dev/null || print_warning "Could not create database"
    fi
else
    print_warning "Skipping database verification (psql not found)"
fi

echo ""

# ============================================
# Step 3: Load Test Data
# ============================================
read -p "Load test data into database? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_step "Loading test data from seed script..."
    SEED_FILE="$JAKEBOOKS_DIR/src/test/resources/seed-test-data.sql"

    if [ -f "$SEED_FILE" ]; then
        if command -v psql &> /dev/null; then
            psql -U "$DB_USER" -h "$DB_HOST" -d "$DB_NAME" -f "$SEED_FILE" 2>/dev/null || \
                print_warning "Some seed data might not have loaded (check for missing tables)"
            print_info "Test data loaded successfully"
        else
            print_error "psql not available, cannot load test data"
        fi
    else
        print_warning "Seed script not found at $SEED_FILE"
    fi
else
    print_info "Skipping test data loading"
fi

echo ""

# ============================================
# Step 4: Build Project
# ============================================
read -p "Build JakeBooks application? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_step "Building JakeBooks application..."
    cd "$JAKEBOOKS_DIR"
    mvn clean package -DskipTests || {
        print_error "Build failed"
        exit 1
    }
    print_info "Build completed successfully"
else
    print_info "Skipping build (assuming already built)"
fi

echo ""

# ============================================
# Step 5: Start Application
# ============================================
print_step "Checking if application is already running..."

if curl -s "$APP_URL/login" > /dev/null 2>&1; then
    print_info "Application is already running at $APP_URL"
else
    read -p "Start JakeBooks application? (y/n) " -n 1 -r
    echo

    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_step "Starting JakeBooks application..."
        cd "$JAKEBOOKS_DIR"

        # Start in background
        nohup mvn spring-boot:run > app.log 2>&1 &
        APP_PID=$!
        print_info "Application started with PID $APP_PID"

        # Wait for app to be ready
        print_info "Waiting for application to start (max $TIMEOUT seconds)..."
        ELAPSED=0
        while ! curl -s "$APP_URL/login" > /dev/null 2>&1; do
            if [ $ELAPSED -ge $TIMEOUT ]; then
                print_error "Application failed to start within $TIMEOUT seconds"
                print_info "Check app.log for details"
                exit 1
            fi
            echo -n "."
            sleep 2
            ELAPSED=$((ELAPSED + 2))
        done
        echo ""
        print_info "Application is ready!"
    else
        print_error "Application must be running for tests to execute"
        print_info "Please start it manually: mvn spring-boot:run"
        exit 1
    fi
fi

echo ""

# ============================================
# Step 6: Run E2E Tests
# ============================================
print_step "Running E2E tests..."
echo ""

cd "$JAKEBOOKS_DIR"

# Ask which test to run
echo "Select which tests to run:"
echo "1) All E2E tests (main + edge cases)"
echo "2) Main test only (successful order placement)"
echo "3) Security tests only"
echo ""
read -p "Enter choice (1-3): " test_choice

case $test_choice in
    1)
        print_step "Running all E2E tests..."
        mvn test -Dtest=OrderPlacementE2ETest
        ;;
    2)
        print_step "Running main E2E test only..."
        mvn test -Dtest=OrderPlacementE2ETest#testSuccessfulOrderPlacement
        ;;
    3)
        print_step "Running security/edge case tests..."
        mvn test -Dtest=OrderPlacementE2ETest#testLoginFailsWithInvalidCredentials \
                      -Dtest=OrderPlacementE2ETest#testCheckoutRequiresAuthentication
        ;;
    *)
        print_error "Invalid choice"
        exit 1
        ;;
esac

TEST_RESULT=$?

echo ""

# ============================================
# Step 7: Results
# ============================================
if [ $TEST_RESULT -eq 0 ]; then
    echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║        ALL TESTS PASSED! ✓             ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════╝${NC}"
else
    echo -e "${RED}╔════════════════════════════════════════╗${NC}"
    echo -e "${RED}║        TESTS FAILED! ✗                 ║${NC}"
    echo -e "${RED}╚════════════════════════════════════════╝${NC}"
    print_info "Check the test output above for details"
fi

echo ""
print_info "Reports available in: target/surefire-reports/"
print_info "Application logs in: app.log"

# ============================================
# Optional: Cleanup
# ============================================
echo ""
read -p "Clean up resources? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    read -p "Kill application process? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        pkill -f "spring-boot:run" || true
        print_info "Application stopped"
    fi
fi

exit $TEST_RESULT
