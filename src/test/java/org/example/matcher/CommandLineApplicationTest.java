package org.example.matcher;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandLineApplicationTest {

    static ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
    static PrintStream originalSystemOut;

    @BeforeAll
    static void captureSystemOut() {
        originalSystemOut = System.out;
        System.setOut(new PrintStream(capturedOutput));
    }

    @AfterAll
    static void restoreSystemOut() {
        System.setOut(originalSystemOut);
    }

    @Test
    @DisplayName("main should process large file with chunking and threading correctly")
    void main_shouldProcessLargeFile() throws InterruptedException {
        String[] args = {
                "--file", "src/test/resources/big.txt",
                "--search", "James,John,Robert,Michael,William,David,Richard,Charles,Joseph,Thomas," +
                "Christopher,Daniel,Paul,Mark,Donald,George,Kenneth,Steven,Edward,Brian,Ronald,Anthony," +
                "Kevin,Jason,Matthew,Gary,Timothy,Jose,Larry,Jeffrey,Frank,Scott,Eric,Stephen,Andrew,Raymond," +
                "Gregory,Joshua,Jerry,Dennis,Walter,Patrick,Peter,Harold,Douglas,Henry,Carl,Arthur,Ryan,Roger",
                "--threads", "4",
                "--chunk", "1000"
        };

        CommandLineApplication.main(args);

        String output = capturedOutput.toString();
        assertNotNull(output);
        assertEquals(39, output.split(System.lineSeparator()).length);
        assertTrue(output.contains("Kenneth         ---> [[lineOffset=45622, charOffset=2757261]]"));
        assertTrue(output.contains("Jason           ---> [[lineOffset=15404, charOffset=1137363]]"));
        assertTrue(output.contains("Timothy         ---> [[lineOffset=13387, charOffset=1018975], [lineOffset=13751, charOffset=1041587]]"));
        assertFalse(output.contains("certainly_not_there"));
    }
}
