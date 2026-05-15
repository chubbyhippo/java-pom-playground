package io.github.chubbyhippo.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommonsCsvTest {

    private static final String[] HEADERS = {"id", "name", "email"};

    @Test
    void testWriteAndReadCsv(@TempDir Path tempDir) throws IOException {
        // given
        var csvFile = tempDir.resolve("people.csv");
        var people = List.of(
                new Person(1, "Alice", "alice@example.com"),
                new Person(2, "Bob", "bob@example.com"),
                new Person(3, "Charlie", "charlie@example.com"));

        // when - write
        writePeople(csvFile, people);

        // then - file exists
        assertThat(csvFile).exists().isRegularFile();

        // when - read
        var loaded = readPeople(csvFile);

        // then - content matches
        assertThat(loaded).containsExactlyElementsOf(people);
    }

    private void writePeople(Path file, List<Person> people) throws IOException {
        var format = CSVFormat.DEFAULT.builder().setHeader(HEADERS).get();

        try (Writer writer = Files.newBufferedWriter(file);
             var printer = new CSVPrinter(writer, format)) {
            for (var p : people) {
                printer.printRecord(p.id(), p.name(), p.email());
            }
        }
    }

    private List<Person> readPeople(Path file) throws IOException {
        var format = CSVFormat.DEFAULT
                .builder()
                .setHeader(HEADERS)
                .setSkipHeaderRecord(true)
                .get();

        try (Reader reader = Files.newBufferedReader(file);
             var parser = format.parse(reader)) {
            return parser.stream()
                    .map(this::toPerson)
                    .toList();
        }
    }

    private Person toPerson(CSVRecord record) {
        return new Person(
                Integer.parseInt(record.get("id")),
                record.get("name"),
                record.get("email"));
    }

    private record Person(int id, String name, String email) {
    }
}