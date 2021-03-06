package edlab.eda.ace;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pool for parallel simulation
 */
public final class EnvironmentPool {

  private final Map<AnalogCircuitEnvironment, EnvironmentThread> sessions;

  public EnvironmentPool() {
    this.sessions = new HashMap<>();
  }

  /**
   * Get all environments in the pool
   * 
   * @return set of {@link AnalogCircuitEnvironment}
   */
  public Set<AnalogCircuitEnvironment> getEnvironemnts() {
    return this.sessions.keySet();
  }

  /**
   * Add an environment to the pool
   * 
   * @param env Environment to be added
   * @return <code>true</code> when the environment was added successfully,
   *         <code>false</code> otherwise
   */
  public boolean add(final AnalogCircuitEnvironment env) {

    if (env instanceof AnalogCircuitEnvironment) {
      this.sessions.put(env, new EnvironmentThread(env));
      return true;
    } else {
      return false;
    }
  }

  /**
   * Run simulation in all environments in the pool in parallel
   * 
   * @return <code>true</code> when successful, <code>false</code> otherwise
   */
  public boolean execute() {
    return this.execute(this.sessions.size());
  }

  /**
   * Run simulation in all environments in the pool in parallel
   * 
   * @param size maximal number of parallel simulations
   * @return <code>true</code> when successful, <code>false</code> otherwise
   */
  public boolean execute(final int size) {
    final ExecutorService executor = Executors.newFixedThreadPool(size);

    for (final Entry<AnalogCircuitEnvironment, EnvironmentThread> entry : this.sessions
        .entrySet()) {
      executor.execute(entry.getValue());
    }

    int i = 0;

    while (i < this.sessions.size()) {

      i = 0;

      try {
        Thread.sleep(0, 1);
      } catch (final InterruptedException e) {
      }

      for (final EnvironmentThread thread : this.sessions.values()) {
        if (thread.isTerminated()) {
          i++;
        }
      }
    }

    executor.shutdown();

    try {
      Thread.sleep(1);
    } catch (final InterruptedException e) {
    }

    while (!executor.isTerminated()) {
      try {
        Thread.sleep(1);
      } catch (final InterruptedException e) {
      }
    }

    return true;
  }
}