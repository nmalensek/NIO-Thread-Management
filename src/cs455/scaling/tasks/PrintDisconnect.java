package cs455.scaling.tasks;

import java.io.IOException;

public class PrintDisconnect implements Task {

    public void perform() throws IOException {
        System.out.println("Client disconnected");
    }
}
