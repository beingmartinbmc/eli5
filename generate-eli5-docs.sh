#!/bin/bash

# ELI5 Documentation Generator Script
# This script makes it easy to generate ELI5 documentation without interfering with the build process

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}ELI5 Documentation Generator${NC}"
echo -e "${BLUE}==========================${NC}"
echo

# Check if we're in a Maven project
if [ ! -f "pom.xml" ]; then
    echo -e "${RED}Error: pom.xml not found. Please run this script from a Maven project root.${NC}"
    exit 1
fi

# Parse command line arguments
SCAN_ONLY=false
SOURCE_DIR="src/main/java"
OUTPUT_FILE="target/eli5-docs/eli5.md"

while [[ $# -gt 0 ]]; do
    case $1 in
        --scan-only)
            SCAN_ONLY=true
            shift
            ;;
        --source-dir)
            SOURCE_DIR="$2"
            shift 2
            ;;
        --output-file)
            OUTPUT_FILE="$2"
            shift 2
            ;;
        --help|-h)
            echo "Usage: $0 [options]"
            echo
            echo "Options:"
            echo "  --scan-only              Only scan for annotations, don't generate docs"
            echo "  --source-dir <dir>       Source directory to scan (default: src/main/java)"
            echo "  --output-file <file>     Output file path (default: target/eli5-docs/eli5.md)"
            echo "  --help, -h               Show this help message"
            echo
            echo "Examples:"
            echo "  $0                                    # Generate docs from src/main/java"
            echo "  $0 --scan-only                        # Only scan for annotations"
            echo "  $0 --source-dir src/test/java         # Generate docs from test sources"
            echo "  $0 --output-file docs/eli5.md         # Custom output location"
            echo
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Check if source directory exists
if [ ! -d "$SOURCE_DIR" ]; then
    echo -e "${RED}Error: Source directory '$SOURCE_DIR' does not exist.${NC}"
    exit 1
fi

# Compile the project first
echo -e "${YELLOW}Compiling project...${NC}"
/opt/homebrew/bin/mvn compile -q

# Run the documentation generator
if [ "$SCAN_ONLY" = true ]; then
    echo -e "${YELLOW}Scanning for @ExplainLikeImFive annotations in: $SOURCE_DIR${NC}"
    java -cp "target/classes:$(find ~/.m2/repository -name "*.jar" | grep -E "(jackson|slf4j)" | head -5 | tr '\n' ':')" \
         io.github.beingmartinbmc.eli5.cli.Eli5DocumentationGenerator scan "$SOURCE_DIR"
else
    echo -e "${YELLOW}Generating ELI5 documentation...${NC}"
    echo -e "${YELLOW}Source directory: $SOURCE_DIR${NC}"
    echo -e "${YELLOW}Output file: $OUTPUT_FILE${NC}"
    echo
    
    # Create output directory if it doesn't exist
    OUTPUT_DIR=$(dirname "$OUTPUT_FILE")
    mkdir -p "$OUTPUT_DIR"
    
    java -cp "target/classes:$(find ~/.m2/repository -name "*.jar" | grep -E "(jackson|slf4j)" | head -5 | tr '\n' ':')" \
         io.github.beingmartinbmc.eli5.cli.Eli5DocumentationGenerator generate "$SOURCE_DIR" "$OUTPUT_FILE"
    
    echo
    echo -e "${GREEN}✅ Documentation generated successfully!${NC}"
    echo -e "${GREEN}📄 Output file: $OUTPUT_FILE${NC}"
fi
