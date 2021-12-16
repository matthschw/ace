package edlab.eda.ace;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EnvironmentPool {

  private Map<AnalogCircuitEnvironment, EnvironmentThread> sessions;

  public EnvironmentPool() {
    this.sessions = new HashMap<AnalogCircuitEnvironment, EnvironmentThread>();
  }

  public void registerSession(AnalogCircuitEnvironment env) {
    EnvironmentThread thread = new EnvironmentThread(env);
    this.sessions.put(env, thread);
  }

  public boolean execute() {

    ExecutorService executor = Executors
        .newFixedThreadPool(this.sessions.size());

    for (Entry<AnalogCircuitEnvironment, EnvironmentThread> entry : sessions
        .entrySet()) {
      executor.execute(entry.getValue());
    }

    int i = 0;

    while (i < this.sessions.size()) {

      i = 0;

      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
      }

      for (EnvironmentThread thread : this.sessions.values()) {
        if (thread.isFinished()) {
          i++;
        }
      }
    }

    executor.shutdown();

    try {
      Thread.sleep(1);
    } catch (InterruptedException e) {
    }

    while (!executor.isTerminated()) {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
      }
    }

    return true;
  }
}