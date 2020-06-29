package org.apache.zookeeper.cli;

import org.apache.commons.cli.*;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentMatchers;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class ExecTest {
    private static CreateCommand command;
    private Class<?> exception;
    private ZooKeeper zk;
    private String newPath;
    private Exception zkException;
    private CommandLine cl;
    private PrintStream err;
    private static Options options;

    @BeforeClass
    public static void setUp() {
        options = new Options();
        options.addOption(new Option("e", false, "ephemeral"));
        options.addOption(new Option("s", false, "sequential"));
        options.addOption(new Option("c", false, "container"));
        options.addOption(new Option("t", true, "ttl"));
    }

    public ExecTest(String[] args, Exception zkException, Class<?> exception) throws CliParseException, KeeperException, InterruptedException, ParseException {
        this.zkException = zkException;
        this.exception = exception;

        Parser parser = new PosixParser();
        cl = parser.parse(options, args);

        zk = getZk(zkException);
        err = mock(PrintStream.class);
        doNothing().when(err).println(isA(String.class));
        command = new CreateCommand();
        command.setErr(err);
        command.setZk(zk);
        command.parse(args);
        if (args.length >= 2)
            this.newPath = args[1];
    }

    public static ZooKeeper getZk(Exception e) throws KeeperException, InterruptedException {
        ZooKeeper zok = mock(ZooKeeper.class);
        when(zok.create(isA(String.class),
                isA(byte[].class), ArgumentMatchers.anyList(), isA(CreateMode.class), isA(Stat.class), isA(long.class)))
                .thenReturn("/zk_test");
        when(zok.create(isA(String.class), any(), ArgumentMatchers.anyList(), isA(CreateMode.class)))
                .thenReturn("/zk_test");
        if (e != null) {
            doThrow(e).when(zok)
                    .create(isA(String.class), isA(byte[].class), ArgumentMatchers.anyList(),
                            isA(CreateMode.class), isA(Stat.class), isA(long.class));
            doThrow(e).when(zok)
                    .create(isA(String.class), isA(byte[].class), ArgumentMatchers.anyList(), isA(CreateMode.class));
        }
        return zok;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> param() {
        return Arrays.asList(new Object[][] {
            {new String[] {"create", "/zk_test", "my_data"}, null, null},
            {new String[] {"create", "/zk_test", "my_data", "-s"}, null, null},
            {new String[] {"create", "/zk_test", "my_data", "-e"}, null, null},
            {new String[] {"create", "/zk_test", "my_data", "-c"}, null, null},
            {new String[] {"create", "/zk_test", "my_data", "-t", "100"}, null, null},
            {new String[] {"create", "/zk_test", "my_data", "-t", "0"}, null, MalformedCommandException.class},
            {new String[] {"create", "/zk_test", "my_data", "-t", "-100"}, null, MalformedCommandException.class},
            {new String[] {"create", "/zk_test", "my_data", "-c", "-e"}, null, MalformedCommandException.class},
            {new String[] {"create", "/zk_test", "my_data", "-c", "-s"}, null, MalformedCommandException.class},
            {new String[] {"create", "/zk_test", "my_data", "-t", "100", "-e"}, null, MalformedCommandException.class},
            {new String[] {"create", "/zk_test", "my_data", "-t", "100", "-c"}, null, MalformedCommandException.class},
            //added after coverage analysis
            {new String[] {"create", "/zk_test", "my_data", "-t", "l"}, null, MalformedCommandException.class},
            {new String[] {"create", "/zk_test", "my_data", "-e", "-s"}, null, null},
            {new String[] {"create", "/zk_test", "my_data", "ip:127.0.0.1:crwda"}, null, null},
            {new String[] {"create", "/zk_test", "my_data", "-t", "100", "-s"}, null, null},
            {new String[] {"create", "/zk_test"}, null, null},

            {new String[] {"create", "/zk_test", "my_data"}, new IllegalArgumentException(), MalformedPathException.class},
            {new String[] {"create", "/zk_test", "my_data"}, new KeeperException.EphemeralOnLocalSessionException(),
                    CliWrapperException.class},
            {new String[] {"create", "/zk_test", "my_data"}, new KeeperException.InvalidACLException(),
                    CliWrapperException.class},
            {new String[] {"create", "/zk_test", "my_data"}, new InterruptedException(), CliWrapperException.class},
        });
    }

    public CreateMode getFlags() {
        if (cl.hasOption("e") && cl.hasOption("s")) {
            return CreateMode.EPHEMERAL_SEQUENTIAL;
        }
        if (cl.hasOption("e")) {
            return CreateMode.EPHEMERAL;
        }
        if (cl.hasOption("s")) {
            return cl.hasOption("t") ? CreateMode.PERSISTENT_SEQUENTIAL_WITH_TTL : CreateMode.PERSISTENT_SEQUENTIAL;
        }
        if (cl.hasOption("c")) {
            return CreateMode.CONTAINER;
        }
        return cl.hasOption("t") ? CreateMode.PERSISTENT_WITH_TTL : CreateMode.PERSISTENT;
    }

    @Test
    public void execTest() throws KeeperException, InterruptedException {
        try {
            assertTrue(command.exec());
            assertNull(exception);
            CreateMode flags = getFlags();
            if (cl.hasOption("t")) {
                verify(zk).create(isA(String.class), isA(byte[].class), ArgumentMatchers.anyList(),
                        eq(flags), isA(Stat.class), isA(long.class));
            } else {
                verify(zk).create(isA(String.class), any(), ArgumentMatchers.anyList(), eq(flags));
            }
            verify(err).println(eq("Created " + newPath));
        } catch (CliException e) {
            assertEquals(exception, e.getClass());
            if (zkException != null) {
                if (zkException.getClass().equals(KeeperException.EphemeralOnLocalSessionException.class)) {
                    verify(err).println("Unable to create ephemeral node on a local session");
                }
                if (zkException.getClass().equals(KeeperException.InvalidACLException.class)) {
                    verify(err).println(zkException.getMessage());
                }
            }
        }
    }
}
