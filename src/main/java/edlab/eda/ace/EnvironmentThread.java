package edlab.eda.ace;

public class EnvironmentThread implements Runnable {

  private final AnalogCircuitEnvironment env;
  public boolean terminated = false;

  public EnvironmentThread(final AnalogCircuitEnvironment env) {
    this.env = env;
  }

  @Override
  public void run() {
    this.env.simulate();
    this.terminated = true;
  }

  public boolean isTerminated() {
    return this.terminated;
  }
}