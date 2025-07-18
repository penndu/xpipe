package io.xpipe.beacon;

import io.xpipe.beacon.api.DaemonStopExchange;
import io.xpipe.core.FilePath;
import io.xpipe.core.OsType;
import io.xpipe.core.XPipeDaemonMode;
import io.xpipe.core.XPipeInstallation;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Contains basic functionality to start, communicate, and stop a remote beacon server.
 */
public class BeaconServer {

    @SneakyThrows
    public static boolean isReachable(int port) {
        var local = Inet4Address.getByAddress(new byte[] {0x7f, 0x00, 0x00, 0x01});

        try (var socket = new Socket()) {
            InetSocketAddress adress = new InetSocketAddress(local, port);
            socket.connect(adress, 5000);
        } catch (Exception e) {
            return false;
        }

        // If there's some kind of networking tool interfering with sockets by for example proxying socket connections
        // The previous connect might succeed even though nothing is running.
        // To be sure, check that the socket is indeed occupied
        try (var ignored = new ServerSocket(port, 0, local)) {
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private static List<String> toProcessCommand(String toExec) {
        // Having the trailing space is very important to force cmd to not interpret surrounding spaces and removing
        // them
        return OsType.getLocal().equals(OsType.WINDOWS)
                ? List.of("cmd", "/c", toExec + " ")
                : List.of("sh", "-c", toExec);
    }

    public static Process tryStartCustom() throws Exception {
        var custom = BeaconConfig.getCustomDaemonCommand();
        if (custom != null) {
            var toExec =
                    custom + (BeaconConfig.getDaemonArguments() != null ? " " + BeaconConfig.getDaemonArguments() : "");
            var command = toProcessCommand(toExec);
            Process process = Runtime.getRuntime().exec(command.toArray(String[]::new));
            printDaemonOutput(process, command);
            return process;
        }
        return null;
    }

    public static Process start(String installationBase, XPipeDaemonMode mode) throws Exception {
        String command;
        if (!BeaconConfig.launchDaemonInDebugMode()) {
            command = XPipeInstallation.createExternalAsyncLaunchCommand(
                    installationBase, mode, BeaconConfig.getDaemonArguments(), false);
        } else {
            command = XPipeInstallation.createExternalLaunchCommand(
                    getDaemonDebugExecutable(installationBase), BeaconConfig.getDaemonArguments(), mode);
        }

        var fullCommand = toProcessCommand(command);
        Process process = new ProcessBuilder(fullCommand).start();
        printDaemonOutput(process, fullCommand);
        return process;
    }

    private static void printDaemonOutput(Process proc, List<String> command) {
        boolean print = BeaconConfig.printDaemonOutput();
        if (print) {
            System.out.println("Starting daemon: " + command);
        }

        var out = new Thread(
                null,
                () -> {
                    try {
                        InputStreamReader isr = new InputStreamReader(proc.getInputStream());
                        BufferedReader br = new BufferedReader(isr);
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (print) {
                                System.out.println("[xpiped] " + line);
                            }
                        }
                    } catch (Exception ioe) {
                        ioe.printStackTrace();
                    }
                },
                "daemon sysout");
        out.setDaemon(true);
        out.start();

        var err = new Thread(
                null,
                () -> {
                    try {
                        InputStreamReader isr = new InputStreamReader(proc.getErrorStream());
                        BufferedReader br = new BufferedReader(isr);
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (print) {
                                System.err.println("[xpiped] " + line);
                            }
                        }
                    } catch (Exception ioe) {
                        ioe.printStackTrace();
                    }
                },
                "daemon syserr");
        err.setDaemon(true);
        err.start();
    }

    public static boolean tryStop(BeaconClient client) throws Exception {
        DaemonStopExchange.Response res =
                client.performRequest(DaemonStopExchange.Request.builder().build());
        return res.isSuccess();
    }

    public static String getDaemonDebugExecutable(String installationBase) {
        var osType = OsType.getLocal();
        var debug = BeaconConfig.launchDaemonInDebugMode();
        if (!debug) {
            throw new IllegalStateException();
        } else {
            if (BeaconConfig.attachDebuggerToDaemon()) {
                return FilePath.of(installationBase, XPipeInstallation.getDaemonDebugAttachScriptPath(osType))
                        .toString();
            } else {
                return FilePath.of(installationBase, XPipeInstallation.getDaemonDebugScriptPath(osType))
                        .toString();
            }
        }
    }
}
