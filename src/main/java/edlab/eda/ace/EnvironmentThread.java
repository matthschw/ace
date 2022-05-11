package edlab.eda.ace;

/**
 * Thread for running an {@link AnalogCircuitEnvironment}
 */
class EnvironmentThread implements Runnable {

  private final AnalogCircuitEnvironment env;
  private boolean terminated = false;

  /**
   * Creating a new thread
   * 
   * @param env Environment
   */
  EnvironmentThread(final AnalogCircuitEnvironment env) {
    this.env = env;
  }

  @Override
  public void run() {
    this.env.simulate();
    this.terminated = true;
  }

  /**
   * Identify if the thread terminted
   * 
   * @return <code>true</code> when thread terminated, <code>false</code>
   *         otherwise
   */
  boolean isTerminated() {
    return this.terminated;
  }
}