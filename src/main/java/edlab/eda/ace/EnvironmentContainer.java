package edlab.eda.ace;

import java.util.HashMap;

public class EnvironmentContainer {

  private static HashMap<Integer, AnalogCircuitEnvironment> envs = new HashMap<Integer, AnalogCircuitEnvironment>();

  public static boolean regEnvironment(int id, AnalogCircuitEnvironment env) {
    return EnvironmentContainer.envs.put(id, env) != null;
  }

  public static AnalogCircuitEnvironment getEnvironment(int id) {
    return EnvironmentContainer.envs.get(id);
  }
}