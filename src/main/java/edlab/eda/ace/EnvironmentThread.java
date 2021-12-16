package edlab.eda.ace;

public class EnvironmentThread implements Runnable {

  private AnalogCircuitEnvironment env;
  public boolean finished = false;

  public EnvironmentThread(AnalogCircuitEnvironment env) {
    this.env = env;
  }

  public void run() {

    this.env.simulate();
    this.finished = true;

  }

  public boolean isFinished() {
    return this.finished;
  }
}