package edlab.eda.ace;

class EnvironmentThread implements Runnable {

  private final AnalogCircuitEnvironment env;
  private boolean terminated = false;

  EnvironmentThread(final AnalogCircuitEnvironment env) {
    this.env = env;
  }

  @Override
  public void run() {
    this.env.simulate();
    this.terminated = true;
  }

  boolean isTerminated() {
    return this.terminated;
  }
}