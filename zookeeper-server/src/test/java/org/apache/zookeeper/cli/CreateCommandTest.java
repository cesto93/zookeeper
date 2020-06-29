package org.apache.zookeeper.cli;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class CreateCommandTest {

    private String[] args;
    private boolean exception;

    public CreateCommandTest(String[] args, boolean exception) {
        this.args = args;
        this.exception = exception;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> param() {
        return Arrays.asList(new Object[][] {
                {new String[] {},  true},
                {new String[] {"", ""}, false},
                {new String[] {"create", "/zk_test"}, false},
                {new String[] {"create", "/zk_test", "my_data"}, false},
                {new String[] {"create", "/zk_test", "my_data", "-s"}, false},
                {new String[] {"create", "/zk_test", "my_data", "-e"}, false},
                {new String[] {"create", "/zk_test", "my_data", "-c"}, false},
                {new String[] {"create", "/zk_test", "my_data", "-t", "100"}, false},
                {new String[] {"create", "/zk_test", "my_data", "-t"}, true},
                {new String[] {"create", "/zk_test", "my_data", "-l"}, true}
        });
    }

    @Test
    public void parseTest() {
        CreateCommand com = new CreateCommand();
        try {
            assertNotNull(com.parse(args));
            assertFalse(exception);
        } catch (CliParseException e) {
            System.out.println(e.getMessage());
            assertTrue(exception);
        }

    }
}
