# Text Matcher

[![codecov](https://codecov.io/gh/kirill-sergeev/text-matcher/branch/main/graph/badge.svg)](https://codecov.io/gh/kirill-sergeev/text-matcher)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=kirill-sergeev_text-matcher&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=kirill-sergeev_text-matcher)


This Java program efficiently searches for multiple string patterns in large text files.
It processes the file concurrently by dividing it into chunks and uses multiple threads to perform the search in parallel.
After processing all chunks, the results are aggregated and printed.

## Features

- Concurrent text processing using multiple threads.
- Efficient multi-pattern search using the Aho-Corasick algorithm.
- Customizable chunk size and thread count.
- Supports case-insensitive search.
- Provides command-line interface.

## Usage

### Building the program

```bash
mvn clean package
```

### Running the Program

```bash
java -jar target/text-matcher-1.0.jar --file <path> --search <terms> [OPTIONS]
```

#### Required Options

- `--file <path>`: Path to the file to process.
- `--search <terms>`: Comma-separated list of search terms.

#### Optional Settings

- `--threads <number>`: Number of threads to use (default is the number of available processors). Valid range is 1 to 100.
- `--chunk <number>`: Number of lines per chunk (default: 1000). Valid range is 1 to 1,000,000.
- `--ignoreCase`: Enables case-insensitive search (default: false).
- `--help`: Displays a help message with usage information.

## Example Output

An example output entry might look like:

```
Timothy         ---> [[lineOffset=13387, charOffset=1018975], [lineOffset=13751, charOffset=1041587]]
Kenneth         ---> [[lineOffset=45622, charOffset=2757261]]
Jason           ---> [[lineOffset=15404, charOffset=1137363]]
```

## Project structure

The project is structured into interfaces and implementation classes:

- CommandLineApplication: Defines entry point and prints results.
- ConfigProvider: Defines configuration settings.
- FileProcessor: Coordinates file reading.
- ResultAggregator: Aggregates partial results.
- TextMatcher: Searches for patterns within text chunks.

```
text-matcher/
├── src/main/java
│   ├── org/example/matcher/
│   │   ├── CommandLineApplication.java
│   │   ├── ConfigProvider.java
│   │   ├── FileProcessor.java
│   │   ├── ResultAggregator.java
│   │   ├── TextMatcher.java
│   │   └── impl/*
├── src/main/resources/
│   └── logback.xml
├── pom.xml
└── README.md
```
