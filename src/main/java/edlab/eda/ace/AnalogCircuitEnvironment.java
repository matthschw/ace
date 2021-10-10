package edlab.eda.ace;

import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import edlab.eda.cadence.rc.spectre.SpectreSession;

public abstract class AnalogCircuitEnvironment {

  public static final String NETLIST_FILE_NAME = "input.scs";
  public static final String JSON_FILE_NAME = "properties.json";

  protected SpectreSession session;
  protected JSONObject jsonObject;
  protected Set<String> blacklistAnalyses;

  public boolean set(String name, double value) {
    return this.session.setValueAttribute(name, value);
  }

  public void set(Map<String, Double> values) {
    for (String name : values.keySet()) {
      set(name, values.get(name));
    }
  }
  
  public abstract void simulate();

}
