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
    private ZooKeeper zk;
    private boolean hasT;

    public ExecTest(String[] args, ZooKeeper zk, boolean exception) throws CliParseException {
        command = new CreateCommand();
        this.zk = zk;
        command.setZk(zk);
        command.parse(args);
        this.exception = exception;
        if(args.length > 3)
            this.hasT = args[3].equals("-t");
        else
            this.hasT = false;
    }

    public static ZooKeeper getZkNoExcept() throws KeeperException, InterruptedException {
        ZooKeeper zok = mock(ZooKeeper.class);
        when(zok.create(isA(String.class),
                isA(byte[].class), ArgumentMatchers.anyList(), isA(CreateMode.class), isA(Stat.class), isA(long.class)))
                .thenReturn("sample path");
        when(zok.create(isA(String.class), isA(byte[].class), ArgumentMatchers.anyList(), isA(CreateMode.class)))
                .thenReturn("sample path");
        return zok;
    }

    public static ZooKeeper getZkExcept(Exception e) throws KeeperException, InterruptedException {
        ZooKeeper zok = mock(ZooKeeper.class);
        when(zok.create(isA(String.class),
                isA(byte[].class), ArgumentMatchers.anyList(), isA(CreateMode.class), isA(Stat.class), isA(long.class)))
                .thenReturn("sample path");
        when(zok.create(isA(String.class), isA(byte[].class), ArgumentMatchers.anyList(), isA(CreateMode.class)))
                .thenReturn("sample path");
        doThrow(e).when(zok)
                .create(isA(String.class), isA(byte[].class), ArgumentMatchers.anyList(),
                        isA(CreateMode.class), isA(Stat.class), isA(long.class));
        doThrow(e).when(zok)
                .create(isA(String.class), isA(byte[].class), ArgumentMatchers.anyList(), isA(CreateMode.class));
        return zok;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> param() throws KeeperException, InterruptedException {
        return Arrays.asList(new Object[][] {
                {new String[] {"create", "/zk_test", "my_data"}, ExecTest.getZkNoExcept(), false},
                {new String[] {"create", "/zk_test", "my_data", "-s"}, ExecTest.getZkNoExcept(), false},
                {new String[] {"create", "/zk_test", "my_data", "-e"}, ExecTest.getZkNoExcept(), false},
                {new String[] {"create", "/zk_test", "my_data", "-c"}, ExecTest.getZkNoExcept(), false},
                {new String[] {"create", "/zk_test", "my_data", "-t", "100"}, ExecTest.getZkNoExcept(), false},
                {new String[] {"create", "/zk_test", "my_data", "-t", "0"}, ExecTest.getZkNoExcept(), true},
                {new String[] {"create", "/zk_test", "my_data", "-t", "-100"}, ExecTest.getZkNoExcept(), true},
                {new String[] {"create", "/zk_test", "my_data", "-c", "-e"}, ExecTest.getZkNoExcept(), true},
                {new String[] {"create", "/zk_test", "my_data", "-c", "-s"}, ExecTest.getZkNoExcept(), true},
                {new String[] {"create", "/zk_test", "my_data", "-t", "100", "-e"}, ExecTest.getZkNoExcept(), true},
                {new String[] {"create", "/zk_test", "my_data", "-t", "100", "-c"}, ExecTest.getZkNoExcept(), true},
                //added after coverage analysis
                {new String[] {"create", "/zk_test", "my_data", "-t", "l"}, ExecTest.getZkNoExcept(), true},
                {new String[] {"create", "/zk_test", "my_data", "-e", "-s"}, ExecTest.getZkNoExcept(), false},
                {new String[] {"create", "/zk_test", "my_data", "ip:127.0.0.1:crwda"}, ExecTest.getZkNoExcept(), false},
                {new String[] {"create", "/zk_test", "my_data", "-t", "100", "-s"}, ExecTest.getZkNoExcept(), false},
                {new String[] {"create", "/zk_test"}, ExecTest.getZkNoExcept(), false},

                {new String[] {"create", "/zk_test", "my_data"}, ExecTest.getZkExcept(new IllegalArgumentException()),
                        true},
                {new String[] {"create", "/zk_test", "my_data"}, ExecTest.getZkExcept(
                        new KeeperException.EphemeralOnLocalSessionException()), true},
                {new String[] {"create", "/zk_test", "my_data"}, ExecTest.getZkExcept(
                        new KeeperException.InvalidACLException()), true},
                {new String[] {"create", "/zk_test", "my_data"}, ExecTest.getZkExcept(
                        new InterruptedException()), true},
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
                verify(zk).create(isA(String.class), any(), ArgumentMatchers.anyList(), isA(CreateMode.class));
            }
        } catch (CliException e) {
            System.out.println(e.getMessage());
            assertTrue(exception);
        }

    }
}
