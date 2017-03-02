package cs455.scaling.tasks.server;

import cs455.scaling.tasks.common.Task;

import java.io.IOException;

public class PrintDisconnect implements Task {

    public void perform() throws IOException {
        System.out.println("Client disconnected");
    }
}
