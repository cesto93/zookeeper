package org.apache.zookeeper.cli;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentMatchers;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class ExecTest {
    private static CreateCommand command;
    private boolean exception;
    private static ZooKeeper zk;
    private boolean hasT;

    public ExecTest(String[] args, boolean exception) throws CliParseException, KeeperException, InterruptedException {
        ExecTest.setUp();
        command.parse(args);
        this.exception = exception;
        if(args.length > 3)
            this.hasT = args[3].equals("-t");
        else
            this.hasT = false;
    }

    public static void setUp() throws KeeperException, InterruptedException {
        command = new CreateCommand();
        zk = mock(ZooKeeper.class);
        when(zk.create(isA(String.class),
                isA(byte[].class), ArgumentMatchers.anyList(), isA(CreateMode.class), isA(Stat.class), isA(long.class)))
                .thenReturn("sample path");
        when(zk.create(isA(String.class), isA(byte[].class), ArgumentMatchers.anyList(), isA(CreateMode.class)))
                .thenReturn("sample path");
        command.setZk(zk);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> param() {
        return Arrays.asList(new Object[][] {
                {new String[] {"create", "/zk_test", "my_data"}, false},
                {new String[] {"create", "/zk_test", "my_data", "-s"}, false},
                {new String[] {"create", "/zk_test", "my_data", "-e"}, false},
                {new String[] {"create", "/zk_test", "my_data", "-c"}, false},
                {new String[] {"create", "/zk_test", "my_data", "-t", "100"}, false},
                {new String[] {"create", "/zk_test", "my_data", "-t", "0"}, true},
                {new String[] {"create", "/zk_test", "my_data", "-t", "-100"}, true},
                {new String[] {"create", "/zk_test", "my_data", "-c", "-e"}, true},
                {new String[] {"create", "/zk_test", "my_data", "-c", "-s"}, true},
                {new String[] {"create", "/zk_test", "my_data", "-t", "100", "-e"}, true},
                {new String[] {"create", "/zk_test", "my_data", "-t", "100", "-c"}, true},
        });
    }

    @Test
    public void execTest() throws KeeperException, InterruptedException {
        try {
            command.exec();
            assertFalse(exception);
            if (hasT) {
                verify(zk).create(isA(String.class), isA(byte[].class), ArgumentMatchers.anyList(),
                        isA(CreateMode.class), isA(Stat.class), isA(long.class));
            } else {
                verify(zk).create(isA(String.class), isA(byte[].class), ArgumentMatchers.anyList(), isA(CreateMode.class));
            }
        } catch (CliException e) {
            System.out.println(e.getMessage());
            assertTrue(exception);
        }

    }
}
