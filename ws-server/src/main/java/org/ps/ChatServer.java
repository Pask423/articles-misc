package org.ps;

import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static org.ps.WebSocketHelper.*;

public class ChatServer {

    private final int port;
    private final Queue<Socket> clients;
    private final ThreadFactory threadFactory;
    private final ScheduledExecutorService pinger;

    public ChatServer(int port) {
        this.port = port;
        this.clients = new ConcurrentLinkedQueue<>();
        this.threadFactory = Thread
                .ofVirtual()
                .name("chat-server")
                .factory();
        this.pinger = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("WebSocket server listening on port %d ...%n", port);

            pinger.scheduleAtFixedRate(this::ping, 20, 30, TimeUnit.SECONDS);

            Socket client;
            while ((client = serverSocket.accept()) != null) {
                System.out.println(client.getInetAddress().getHostAddress() + ":" + client.getPort());
                threadFactory.newThread(handleClient(client)).start();
            }
        }
    }

    private void ping() {
        for (Socket client : clients) {
            if (client.isClosed()) {
                clients.remove(client);
            } else {
                try {
                    send(("Ping " + LocalDateTime.now()).getBytes(), true, client, Opcode.OPCODE_PING);
                } catch (Exception e) {
                    stopClient(client);
                }
            }
        }
    }

    private Runnable handleClient(Socket client) {
        return () -> {
            try {
                if (!handshake(client)) {
                    client.close();
                } else {
                    clients.add(client);
                    FragmentAssembler fragmentAssembler = new FragmentAssembler();
                    while (true) {
                        WebSocketFrame frame = readFrame(client);
                        switch (frame.opCode()) {
                            case OPCODE_PING -> send(frame.payload(), true, client, Opcode.OPCODE_PONG);
                            case OPCODE_PONG -> System.out.println("Newest Ping" + new String(frame.payload()));
                            case OPCODE_TEXT, OPCODE_BINARY, OPCODE_CONT ->
                                    handleFrame(client, fragmentAssembler, frame);
                            case OPCODE_CLOSE -> {
                                send(("Bye " + LocalDateTime.now()).getBytes(), true, client, Opcode.OPCODE_CLOSE);
                                stopClient(client);
                            }
                            case UNMASKED, UNKNOWN -> stopClient(client);
                        }
                    }
                }
            } catch (Exception e) {
                stopClient(client);
            }
        };

    }

    private void handleFrame(Socket currentClient, FragmentAssembler fragmentAssembler, WebSocketFrame frame) throws IOException {
        AssembledMessage msg = fragmentAssembler.accept(frame);
        Opcode opcode = msg.type().opcode();
        if (opcode != Opcode.UNKNOWN) {
            for (Socket client : clients) {
                if (client != currentClient) {
                    if (client.isClosed()) {
                        clients.remove(client);
                    } else {
                        send(msg.data(), frame.fin(), client, opcode);
                    }
                }
            }
        } else if (msg.type() == Type.ERROR) {
            stopClient(currentClient);
        }
    }

    private void stopClient(Socket client) {
        try {
            clients.remove(client);
            client.close();
        } catch (IOException ignored) {

        }
    }

    public static void main(String[] args) throws IOException {
        ChatServer chatServer = new ChatServer(8080);
        chatServer.start();
    }
}