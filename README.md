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
    <version>1.0.4</version>
    <scope>provided</scope>
</dependency>
```

**Quick Test**: If you want to try it immediately without an API key, the plugin will use a stub service and generate placeholder explanations.

### 2. Configure OpenAI API Key

**Option A: Environment Variable (Recommended for CI/CD)**
```bash
export ELI5_OPENAI_APIKEY=sk-your-actual-api-key-here
```

**Option B: System Properties (Good for testing)**
```bash
mvn eli5:generate-docs -Deli5.openai.apiKey=sk-your-actual-api-key-here
```

**Option C: Properties File (Good for development)**
Create `src/main/resources/eli5.properties` in your project:

```properties
# OpenAI Configuration
eli5.openai.apiKey=sk-your-actual-api-key-here
eli5.openai.model=gpt-4.1-nano
eli5.openai.maxTokens=2000
eli5.openai.temperature=0.5

# General Configuration
eli5.outputMode=markdown
eli5.outputPath=target/eli5-docs
eli5.ai.service=openai
```

**⚠️ Security Note**: Never commit API keys to version control. Add `*.properties` to your `.gitignore` file.

**When to use each option:**
- **Environment Variables**: Best for CI/CD pipelines and production deployments
- **Properties File**: Best for local development and team collaboration
- **System Properties**: Best for quick testing and one-off runs

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
# Generate full documentation
mvn eli5:generate-docs

# Generate from specific source directory
mvn eli5:generate-docs -Deli5.sourceDirectory=src/main/java

# Custom output location
mvn eli5:generate-docs -Deli5.outputFile=docs/eli5.md
```

The documentation will be generated in `target/eli5-docs/eli5.md` by default.

## Complete Integration Example

Here's a complete `pom.xml` example showing how to integrate ELI5:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>my-project</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.release>11</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- ELI5 annotation dependency -->
        <dependency>
            <groupId>io.github.beingmartinbmc</groupId>
            <artifactId>eli5</artifactId>
            <version>1.0.4</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <release>11</release>
                </configuration>
            </plugin>
            
            <!-- ELI5 Maven Plugin -->
            <plugin>
                <groupId>io.github.beingmartinbmc</groupId>
                <artifactId>eli5</artifactId>
                <version>1.0.4</version>
                <configuration>
                    <sourceDirectory>src/main/java</sourceDirectory>
                    <outputFile>target/eli5-docs/eli5.md</outputFile>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

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
mvn eli5:generate-docs

# Custom source directory
mvn eli5:generate-docs -Deli5.sourceDirectory=src/test/java

# Custom output file
mvn eli5:generate-docs -Deli5.outputFile=docs/eli5.md

# Combine options
mvn eli5:generate-docs \
  -Deli5.sourceDirectory=src/main/java \
  -Deli5.outputFile=docs/main-eli5.md
```

### Plugin Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `eli5.sourceDirectory` | `src/main/java` | Source directory to scan for annotations |
| `eli5.outputFile` | `target/eli5-docs/eli5.md` | Output file path |

### Performance Benefits

- **Batch Processing**: Single API call for all annotations (much faster)
- **Build Independence**: No impact on compilation time
- **Optional Generation**: Generate docs only when needed

## Configuration Options

### Environment Variables

- `ELI5_OPENAI_APIKEY`: Your OpenAI API key
- `ELI5_OPENAI_MODEL`: OpenAI model (default: gpt-4.1-nano)
- `ELI5_OUTPUT_MODE`: Output format (default: markdown)
- `ELI5_OUTPUT_PATH`: Output directory (default: target/eli5-docs)

### Properties File (Caller Service Responsibility)

The ELI5 library looks for configuration in `src/main/resources/eli5.properties` in your project. This allows each consuming service to maintain its own configuration without affecting the main library.

**Example `src/main/resources/eli5.properties`:**
```properties
# OpenAI Configuration
eli5.openai.apiKey=your-openai-api-key-here
eli5.openai.model=gpt-4.1-nano
eli5.openai.maxTokens=2000
eli5.openai.temperature=0.5

# General Configuration
eli5.outputMode=markdown
eli5.outputPath=target/eli5-docs
eli5.ai.service=openai
```

**Important Notes:**
- Each project using ELI5 should maintain its own `eli5.properties` file
- The properties file should be in `src/main/resources/eli5.properties`
- **⚠️ CRITICAL**: Add `*.properties` to your `.gitignore` to avoid committing API keys
- For testing, you can create `src/test/resources/eli5.properties` with test configuration
- API keys should start with `sk-` (OpenAI format)

### System Properties

You can also pass configuration via Maven system properties:

```bash
mvn eli5:generate-docs \
  -Deli5.openai.apiKey=sk-your-api-key-here \
  -Deli5.openai.model=gpt-4.1-nano \
  -Deli5.outputMode=markdown
```

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

*Generated on 2025-08-19 23:49:59*

This documentation explains the code in simple terms, as if explaining to a 5-year-old.

## Table of Contents

- [factorial](#factorial)
- [PI](#pi)

## Method: factorial

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

## Requirements

- Java 11 or higher
- Maven 3.6 or higher
- OpenAI API key (optional, for real explanations)

## Building from Source

```bash
git clone <repository-url>
cd eli5
mvn clean install -DskipTests=true -Dgpg.skip=true
```

## Project Structure

```
eli5/
├── src/
│   └── main/java/io/github/beingmartinbmc/eli5/
│       ├── annotations/     # @ExplainLikeImFive annotation
│       ├── ai/             # AI service implementations
│       ├── config/         # Configuration management
│       ├── output/         # Documentation writers
│       ├── processor/      # Annotation processor
│       └── maven/          # Maven plugin implementation
└── pom.xml
```

## Features

✅ **Maven Plugin**: Clean, separate documentation generation
✅ **Batch Processing**: Single API call for all annotations (fast & efficient)
✅ **Build Independence**: No impact on compilation time
✅ **OpenAI Integration**: Real AI-powered explanations
✅ **Fallback Support**: Stub service when API key unavailable
✅ **Markdown Output**: Clean, readable documentation
✅ **Flexible Configuration**: Environment variables, system properties, or properties files
✅ **Security**: Caller services maintain their own configuration files
✅ **Easy Integration**: Simple dependency and plugin configuration

## Troubleshooting

### Common Issues

1. **"No SLF4J providers were found"**: This is normal - the plugin works without logging providers
2. **"Could not find goal"**: Use `eli5:generate-docs` (not `eli5:documentation`)
3. **API key not found**: Set `ELI5_OPENAI_APIKEY` environment variable, use `-Deli5.openai.apiKey=sk-your-key`, or create `src/main/resources/eli5.properties`
4. **Configuration not loaded**: Ensure your `eli5.properties` file is in `src/main/resources/` directory

### Getting Help

- Check that your OpenAI API key is valid and has sufficient credits
- Ensure you're using Java 11 or higher
- Verify the plugin version matches the dependency version

### Security Best Practices

- **Never commit API keys** to version control
- **Use environment variables** in production environments
- **Add `*.properties`** to your `.gitignore` file
- **Rotate API keys** regularly
- **Use test API keys** for development and testing

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 Ankit Sharma

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Contributing

We welcome contributions to ELI5! Here's how you can help:

### How to Contribute

1. **Fork the repository** on GitHub
2. **Create a feature branch** from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes** following our coding standards
4. **Test your changes** thoroughly
5. **Commit your changes** with clear commit messages:
   ```bash
   git commit -m "feat: add new AI service support"
   git commit -m "fix: resolve configuration loading issue"
   git commit -m "docs: update README with new examples"
   ```
6. **Push to your fork** and create a Pull Request

### Development Setup

1. **Clone your fork**:
   ```bash
   git clone https://github.com/your-username/eli5.git
   cd eli5
   ```

2. **Set up development environment**:
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 11)
   export PATH="/opt/homebrew/bin:$PATH"
   ```

3. **Build the project**:
   ```bash
   mvn clean install -DskipTests=true -Dgpg.skip=true
   ```

4. **Test your changes**:
   ```bash
   # Test in a sample project
   cd /path/to/test-project
   mvn eli5:generate-docs
   ```

### Coding Standards

- **Java 11+** compatibility
- **Maven** for build management
- **Clear commit messages** following conventional commits
- **Documentation** for new features
- **Test coverage** for bug fixes and new features

### Pull Request Guidelines

- **Clear description** of what the PR does
- **Reference issues** if applicable
- **Include tests** for new functionality
- **Update documentation** if needed
- **Ensure CI passes** before requesting review

### Issue Reporting

When reporting issues, please include:

- **ELI5 version** you're using
- **Java version** and **Maven version**
- **Steps to reproduce** the issue
- **Expected vs actual behavior**
- **Error messages** or logs
- **Environment details** (OS, etc.)

### Areas for Contribution

- **New AI service integrations** (Claude, Gemini, etc.)
- **Additional output formats** (HTML, PDF, etc.)
- **Performance improvements**
- **Documentation enhancements**
- **Bug fixes**
- **Test coverage improvements**

### Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Help others learn and grow
- Follow the project's coding standards

Thank you for contributing to ELI5! 🚀
