# ELI5 Annotations

Explain Like I'm 5 (ELI5) annotations for Java code documentation. This project provides annotations that automatically generate simple, easy-to-understand explanations of Java code using AI services.

## What It Does

The `@ExplainLikeImFive` annotation automatically generates child-friendly explanations of your Java code using AI services. It uses a Maven plugin approach for clean, fast documentation generation without interfering with your build process.

## Quick Start

### 1. Add Dependency

Add this to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.beingmartinbmc</groupId>
    <artifactId>eli5</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Configure OpenAI API Key

**Option A: Properties File (Recommended for development)**

Copy the template and configure:
```bash
cp eli5.properties.template src/main/resources/eli5.properties
# Edit the file and add your actual API key
```

**Option B: Environment Variable (Recommended for production)**
```bash
export ELI5_OPENAI_APIKEY=your-actual-api-key
```

**Get your API key from**: https://platform.openai.com/account/api-keys

### 3. Use the Annotation

```java
import io.github.beingmartinbmc.eli5.annotations.ExplainLikeImFive;

public class MathUtils {
    
    @ExplainLikeImFive(prompt = "This method calculates factorial using recursion")
    public int factorial(final int n) {
        return n <= 1 ? 1 : n * factorial(n - 1);
    }
    
    @ExplainLikeImFive
    public static final double PI = 3.14159265359;
}
```

### 4. Generate Documentation

Use the Maven plugin to generate documentation:

```bash
# Scan for annotations only
mvn io.github.beingmartinbmc:eli5:1.0.0:generate-docs -Deli5.scanOnly=true

# Generate full documentation
mvn io.github.beingmartinbmc:eli5:1.0.0:generate-docs

# Generate from specific source directory
mvn io.github.beingmartinbmc:eli5:1.0.0:generate-docs -Deli5.sourceDirectory=src/main/java

# Custom output location
mvn io.github.beingmartinbmc:eli5:1.0.0:generate-docs -Deli5.outputFile=docs/eli5.md
```

The documentation will be generated in `target/eli5-docs/eli5.md` by default.

## Annotation Usage

### Basic Usage

```java
@ExplainLikeImFive
public void simpleMethod() {
    // method implementation
}
```

### With Custom Prompt

```java
@ExplainLikeImFive(prompt = "This method sorts an array using bubble sort algorithm")
public void bubbleSort(int[] arr) {
    // implementation
}
```

### Exclude Method Body

```java
@ExplainLikeImFive(includeBody = false, prompt = "This method validates user input")
public boolean validateInput(String input) {
    // complex validation logic
}
```

### On Classes

```java
@ExplainLikeImFive(prompt = "This is a utility class for string operations")
public class StringUtils {
    // class methods
}
```

### On Fields

```java
@ExplainLikeImFive
public static final int MAX_RETRY_ATTEMPTS = 3;
```

## Maven Plugin Usage

The ELI5 project provides a Maven plugin for generating documentation without interfering with your build process.

### Plugin Commands

```bash
# Basic usage - scan and generate from default source directory
mvn io.github.beingmartinbmc:eli5:1.0.0:generate-docs

# Scan only - just find annotated elements without generating docs
mvn io.github.beingmartinbmc:eli5:1.0.0:generate-docs -Deli5.scanOnly=true

# Custom source directory
mvn io.github.beingmartinbmc:eli5:1.0.0:generate-docs -Deli5.sourceDirectory=src/test/java

# Custom output file
mvn io.github.beingmartinbmc:eli5:1.0.0:generate-docs -Deli5.outputFile=docs/eli5.md

# Combine options
mvn io.github.beingmartinbmc:eli5:1.0.0:generate-docs \
  -Deli5.sourceDirectory=src/main/java \
  -Deli5.outputFile=docs/main-eli5.md
```

### Plugin Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `eli5.sourceDirectory` | `src/main/java` | Source directory to scan for annotations |
| `eli5.outputFile` | `target/eli5-docs/eli5.md` | Output file path |
| `eli5.scanOnly` | `false` | Only scan for annotations, don't generate docs |

### Performance Benefits

- **Batch Processing**: Single API call for all annotations (much faster)
- **Build Independence**: No impact on compilation time
- **Optional Generation**: Generate docs only when needed

## Configuration Options

### Properties File (`src/main/resources/eli5.properties`)

```properties
# General Configuration
eli5.outputMode=markdown
eli5.outputPath=target/eli5-docs
eli5.ai.service=openai

# OpenAI Configuration
eli5.openai.apiKey=your-openai-api-key-here
eli5.openai.model=gpt-4.1-nano
eli5.openai.maxTokens=2000
eli5.openai.temperature=0.5
```

### Environment Variables

- `ELI5_OPENAI_APIKEY`: Your OpenAI API key (note: no underscore between API and KEY)
- `ELI5_OPENAI_MODEL`: OpenAI model (default: gpt-4.1-nano)
- `ELI5_OUTPUT_MODE`: Output format (default: markdown)
- `ELI5_OUTPUT_PATH`: Output directory (default: target/eli5-docs)

## How It Works

1. **Annotation Scanning**: The Maven plugin scans your code for `@ExplainLikeImFive` annotations
2. **Batch Processing**: Collects all annotations and sends them in a single API call for efficiency
3. **AI Processing**: Sends code to OpenAI API for explanation
4. **Documentation Generation**: Creates markdown files with simple explanations
5. **Fallback**: Uses stub service if no API key is available
6. **Build Independence**: Documentation generation doesn't interfere with your build process

## Example Output

The generated documentation includes:

```markdown
# ELI5 Documentation

## Method: factorial(int n)

**Code:**
```java
public int factorial(final int n) {
    return n <= 1 ? 1 : n * factorial(n - 1);
}
```

**Explanation:**
Imagine you have a bunch of blocks, and you want to find out how many different ways you can stack them. The factorial is like counting all those ways, but in a special way.

Here's how it works:
- If you have just 1 block or no blocks, there's only 1 way to do it
- If you have more blocks, you multiply that number by the ways to stack fewer blocks
- It keeps asking itself to solve smaller parts until it's easy to finish
```

## Testing

### Run Tests

```bash
mvn test
```

The tests work with or without an OpenAI API key:
- **With API key**: Uses real OpenAI service for explanations
- **Without API key**: Uses stub service with placeholder explanations

### Test Configuration

Tests use `src/test/resources/eli5.properties`:

```properties
eli5.openai.apiKey=your-test-api-key
eli5.outputMode=markdown
eli5.outputPath=target/test-eli5-docs
eli5.test.useStubIfNoKey=true
```

## Requirements

- Java 11 or higher
- Maven 3.6 or higher
- OpenAI API key (optional, for real explanations)

## Building from Source

```bash
git clone <repository-url>
cd eli5
mvn clean install
```

## Project Structure

```
eli5/
├── src/
│   ├── main/java/io/github/beingmartinbmc/eli5/
│   │   ├── annotations/     # @ExplainLikeImFive annotation
│   │   ├── ai/             # AI service implementations
│   │   ├── config/         # Configuration management
│   │   ├── output/         # Documentation writers
│   │   └── processor/      # Annotation processor
│   └── test/               # Test classes and resources
└── pom.xml
```

## Features

✅ **Maven Plugin**: Clean, separate documentation generation
✅ **Batch Processing**: Single API call for all annotations (fast & efficient)
✅ **Build Independence**: No impact on compilation time
✅ **OpenAI Integration**: Real AI-powered explanations
✅ **Fallback Support**: Stub service when API key unavailable
✅ **Markdown Output**: Clean, readable documentation
✅ **Flexible Configuration**: Properties file or environment variables
✅ **Comprehensive Testing**: Works with or without API key
✅ **Security**: Template-based configuration prevents API key leaks

## License

[Add your license here]

## Contributing

[Add contribution guidelines here]
# eli5
