package cs455.scaling.tasks.server;

import cs455.scaling.tasks.common.Task;

import java.io.IOException;

/**
 * Prevents all threads in the thread pool from blocking
 * if a client disconnects (new task is added so notifyAll() is called).
 */

public class PrintDisconnect implements Task {

    public void perform() throws IOException {
        System.out.println("Client disconnected");
    }
}
