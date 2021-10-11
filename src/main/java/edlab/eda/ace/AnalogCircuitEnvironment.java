package edlab.eda.ace;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONObject;

import edlab.eda.cadence.rc.spectre.SpectreFactory;
import edlab.eda.cadence.rc.spectre.SpectreSession;

/**
 * Environment for characterization of an analog circuit
 */
public abstract class AnalogCircuitEnvironment {

  public static final String NETLIST_FILE_NAME = "input.scs";
  public static final String JSON_FILE_NAME = "properties.json";

  public static final String PARAMETERS_ID = "parameters";
  public static final String PERFORMANCES_ID = "performances";
  public static final String REFERENCE_ID = "reference";

  protected SpectreSession session;
  protected JSONObject jsonObject;
  protected Set<String> blacklistAnalyses;

  protected Map<String, Parameter> parameters;

  protected Map<String, Double> parameterValues;
  protected Map<String, Double> performanceValues;

  protected AnalogCircuitEnvironment(SpectreFactory factory,
      JSONObject jsonObject, String netlist, File[] includeDirs,
      Set<String> blacklistAnalyses) {

    this.jsonObject = jsonObject;

    this.session = factory.createSession();

    for (File file : includeDirs) {

      try {

        this.session.addIncludeDirectory(file);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }

    this.session.setNetlist(netlist);

    this.parameterValues = new HashMap<String, Double>();
    this.performanceValues = new HashMap<String, Double>();
    this.parameters = new HashMap<String, Parameter>();

    this.blacklistAnalyses = blacklistAnalyses;

    JSONObject parametersJsonObject = this.jsonObject
        .getJSONObject(PARAMETERS_ID);

    Iterator<String> iterator = parametersJsonObject.keys();
    String name;

    Parameter parameter;

    while (iterator.hasNext()) {

      name = iterator.next();
      parameter = Parameter.get(name, parametersJsonObject.getJSONObject(name));

      this.parameters.put(parameter.getName(), parameter);
      this.set(name, parameter.getInit());

    }
  }

  /**
   * Trigger a circuit simulation
   * 
   * @return
   */
  public abstract AnalogCircuitEnvironment simulate();

  /**
   * Stop the environment. When the method is not called, the environment is
   * stopped automatically when a timeout 15min with no action is exceeded.
   */
  public void stop() {
    this.session.stop();
  }

  /**
   * Get a map of all parameters in the design. The key of the map corresponds
   * to the name of the parameter and the value is the corresponding parameter
   * handle of class {@link Parameter}.
   * 
   * @return map of parameters
   */
  public Map<String, Parameter> getParameters() {
    return this.parameters;
  }

  /**
   * Get a map of all sizing parameters in the design. The key of the map
   * corresponds to the name of the parameter and the value is the corresponding
   * parameter handle of class {@link Parameter}.
   * 
   * @return map of parameters
   * @see Parameter#isSizingParameter()
   */
  public Map<String, Parameter> getSizingParameters() {

    Map<String, Parameter> retval = new HashMap<String, Parameter>();

    for (Entry<String, Parameter> entry : this.parameters.entrySet()) {
      retval.put(entry.getKey(), entry.getValue());
    }

    return retval;
  }

  /**
   * Get the parameter values that are currently set in the circuit
   * 
   * @return map of parameter names and parameter values
   */
  public Map<String, Double> getParameterValues() {
    return this.parameterValues;
  }

  /**
   * Get the performance values from the last simulation as a map. The key
   * corresponds
   * 
   * @return map of performances
   */
  public Map<String, Double> getPerformanceValues() {
    return this.performanceValues;
  }

  /**
   * Set a parameter in the circuit to a specific value
   * 
   * @param name  Name of the parameter
   * @param value New value of the parameter
   * @return true
   */
  public boolean set(String name, double value) {

    if (this.parameters.containsKey(name)) {

      value = this.parameters.get(name).getValidValue(value);

      this.parameterValues.put(name, value);

      return this.session.setValueAttribute(name, value);

    } else {

      System.out.println(name + " is not a parameter");
      return false;
    }
  }

  /**
   * Set multiple parameters in the circuit
   * 
   * @param values map of values. The key corresponds to the name of the
   *               parameter and the value to the new value of the parameter
   * @return <code>true</code> when all parameters are set correctly,
   *         <code>false</code> otherwise
   */
  public boolean set(Map<String, Double> values) {

    for (String name : values.keySet()) {

      if (!this.set(name, values.get(name))) {

        return false;
      }
    }

    return true;
  }

  /**
   * Get a map of random sizing parameters in the design. The key of the map
   * corresponds to the name of the parameter and a random value.
   * 
   * @return map of parameters
   * @see Parameter#isSizingParameter()
   */
  public Map<String, Double> getRandomSizingParameters() {

    Map<String, Double> retval = new HashMap<String, Double>();

    for (Entry<String, Parameter> entry : this.parameters.entrySet()) {

      if (entry.getValue().isSizingParameter()) {
        retval.put(entry.getKey(), entry.getValue().getRandom());
      }
    }

    return retval;
  }
}