package nonblocking.scaling.tasks.common;

import java.io.IOException;

public interface Task {

    void perform() throws IOException;
}
