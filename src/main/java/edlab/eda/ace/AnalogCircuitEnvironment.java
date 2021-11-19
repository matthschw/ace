package edlab.eda.ace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONObject;

import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.cadence.rc.spectre.SpectreFactory;
import edlab.eda.cadence.rc.spectre.SpectreSession;
import edlab.eda.simulation.spectre.concurrent.ParallelizableSession;
import edlab.eda.simulation.spectre.concurrent.SingleSpectreSession;
import edlab.eda.simulation.spectre.concurrent.SpectreParallelExecuterFramework;

/**
 * Environment for characterization of an analog circuit
 */
public abstract class AnalogCircuitEnvironment {

  public static final String NOMINAL_DEFAULT = "nom";

  public static final String NETLIST_FILE_NAME = "input.scs";
  public static final String JSON_FILE_NAME = "properties.json";

  public static final String PARAMETERS_ID = "parameters";
  public static final String PERFORMANCES_ID = "performances";
  public static final String REFERENCE_ID = "reference";
  public static final String CORNERS_ID = "reference";
  public static final String NETLIST_ID = "netlist";

  protected Map<String, SpectreSession> sessions = new HashMap<String, SpectreSession>();

  protected JSONObject jsonObject;

  protected Map<String, String> corners = new HashMap<String, String>();

  protected Map<String, ParallelizableSession> parallelizableSessions = new HashMap<String, ParallelizableSession>();

  protected Map<String, Parameter> parameters;
  protected Map<String, Double> parameterValues;
  
  protected Map<String, HashMap<String, Double>> performanceValues;

  protected Map<String, String> errorMessage;
  private SpectreFactory factory;
  private File[] includeDirs;
  private File dir;

  private String nomCorner = null;

  protected AnalogCircuitEnvironment(SpectreFactory factory,
      JSONObject jsonObject, File dir, File[] includeDirs) {

    this.factory = factory;
    this.includeDirs = includeDirs;
    this.dir = dir;
    this.jsonObject = jsonObject;

    Iterator<String> iterator;
    String name;

    if (this.jsonObject.has(CORNERS_ID)) {

      JSONObject cornersJsonObject = this.jsonObject.getJSONObject(CORNERS_ID);
      iterator = cornersJsonObject.keys();

      while (iterator.hasNext()) {

        name = iterator.next();
        this.corners.put(name,
            cornersJsonObject.getJSONObject(name).getString(NETLIST_ID));

        if (this.nomCorner == null) {
          this.nomCorner = name;
        }
      }
    } else {
      this.corners.put(NOMINAL_DEFAULT, NETLIST_FILE_NAME);
      this.nomCorner = NOMINAL_DEFAULT;
    }

    this.parameterValues = new HashMap<String, Double>();
    this.performanceValues = new HashMap<String, Double>();
    this.parameters = new HashMap<String, Parameter>();

    this.errorMessage = new HashMap<String, String>();

    JSONObject parametersJsonObject = this.jsonObject
        .getJSONObject(PARAMETERS_ID);

    iterator = parametersJsonObject.keys();

    Parameter parameter;

    while (iterator.hasNext()) {

      name = iterator.next();
      parameter = Parameter.get(name, parametersJsonObject.getJSONObject(name));

      this.parameters.put(parameter.getName(), parameter);
      this.set(name, parameter.getInit());
    }
  }

  private void allocateSessions(Set<String> corners) {

    SpectreSession session;

    for (String corner : corners) {

      if (!this.sessions.containsKey(corner)) {

        session = factory.createSession(corner);

        for (File file : this.includeDirs) {
          try {
            session.addIncludeDirectory(file);
          } catch (FileNotFoundException e) {
            e.printStackTrace();
          }
        }

        try {
          session.addIncludeDirectory(this.dir);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }

        try {
          session.setNetlist(new File(this.dir, this.corners.get(corner)));
        } catch (IOException e) {
          e.printStackTrace();
        }

        this.sessions.put(corner, session);
        this.parallelizableSessions.put(corner,
            new SingleSpectreSession(session));

        for (Entry<String, Double> entry : this.parameterValues.entrySet()) {
          try {
            session.setValueAttribute(entry.getKey(), entry.getValue());
          } catch (UnableToStartSession e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  /**
   * Trigger a circuit simulation
   * 
   * @param blacklistAnalyses set of analyses to be ignored
   * 
   * @return <code>this</code>
   */
  public AnalogCircuitEnvironment simulate(Set<String> blacklistAnalyses,
      Set<String> corners) {

    if (corners == null || corners.isEmpty()) {
      corners = new HashSet<String>();
      corners.add(this.nomCorner);
    }

    this.allocateSessions(corners);

    SpectreParallelExecuterFramework framework = new SpectreParallelExecuterFramework(
        this.corners.size());
    
    for (ParallelizableSession session : this.parallelizableSessions.values()) {
      framework.registerSession(session);
    }
    
    try {
      framework.start();
    } catch (UnableToStartSession e) {
      e.printStackTrace();
      return null;
    }

    return this;
  }

  /**
   * Trigger a circuit simulation
   * 
   * @param blacklistAnalyses set of analyses to be ignored
   * 
   * @return <code>this</code>
   */
  public AnalogCircuitEnvironment simulate(Set<String> blacklistAnalyses) {
    HashSet<String> corners = new HashSet<String>();
    corners.add(this.nomCorner);

    return this.simulate(new HashSet<String>(), corners);
  }

  /**
   * Trigger a circuit simulation
   * 
   * @return <code>this</code>
   */
  public AnalogCircuitEnvironment simulate() {
    HashSet<String> corners = new HashSet<String>();
    corners.add(this.nomCorner);

    return this.simulate(new HashSet<String>(), corners);
  }

  /**
   * Stop the environment. When the method is not called, the environment is
   * stopped automatically when a timeout 15min with no action is exceeded.
   */
  public void stop() {

    for (SpectreSession session : this.sessions.values()) {
      session.stop();
    }
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

      for (Entry<String, SpectreSession> entry : this.sessions.entrySet()) {

        try {
          entry.getValue().setValueAttribute(name, value);
        } catch (UnableToStartSession e) {
          e.printStackTrace();
          return false;
        }
      }

      return true;

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

  /**
   * Get a map of the initial sizing parameters in the design. The key of the
   * map corresponds to the name of the parameter and a random value.
   * 
   * @return map of parameters
   * @see Parameter#isSizingParameter()
   */
  public Map<String, Double> getInitialSizingParameters() {

    Map<String, Double> retval = new HashMap<String, Double>();

    for (Entry<String, Parameter> entry : this.parameters.entrySet()) {

      if (entry.getValue().isSizingParameter()) {

        retval.put(entry.getKey(), entry.getValue().getInit());
      }
    }

    return retval;
  }

  /**
   * Get all analyses in the environment
   * 
   * @return set of analyses
   */
  public Set<String> getAnalyses() {
    Set<String> retval = new HashSet<String>();

    Iterator<String> analysesIterator = this.jsonObject
        .getJSONObject(PERFORMANCES_ID).keys();

    while (analysesIterator.hasNext()) {
      retval.add(analysesIterator.next());
    }

    return retval;
  }

  /**
   * Get all performance identifiers
   * 
   * @return set of performance identifiers
   */
  public Set<String> getPerformanceIdentifiers() {
    return this.getPerformanceIdentifiers(new HashSet<String>());
  }

  /**
   * Get the status of the environment (all parameters) as a JSON object
   * 
   * @return JSON object
   */
  public JSONObject getStatus() {

    JSONObject jsonObj = new JSONObject();

    for (Entry<String, Double> entry : this.getParameterValues().entrySet()) {
      jsonObj.put(entry.getKey(), entry.getValue());
    }

    return jsonObj;
  }

  /**
   * Set the status of the environment based on a JSON object
   * 
   * @param jsonObj JSON object
   * 
   * @return <code>true</code> when the status of the environment was set
   *         correctly, <code>false</code> otherwise
   */
  public boolean setStatus(JSONObject jsonObj) {

    for (String key : this.parameters.keySet()) {

      if (jsonObj.getDouble(key) != Double.NaN) {

        this.set(key, jsonObj.getDouble(key));

      } else {
        return false;
      }
    }

    return true;
  }

  /**
   * Set the status of the environment based on a JSON file
   * 
   * @param file Path to JSON
   * 
   * @return <code>true</code> when the status of the environment was set
   *         correctly, <code>false</code> otherwise
   */
  public boolean setStatus(String file) {

    File jsonFile = new File(file);

    if (!(jsonFile.exists() && jsonFile.canRead())) {

      System.err.println("Cannot read file \"" + jsonFile.toString() + "\"");

      return false;
    }

    JSONObject jsonObj;

    try {

      jsonObj = new JSONObject(
          new String(Files.readAllBytes(jsonFile.toPath())));
    } catch (Exception e) {
      System.err.println("Cannot read JSON \"" + jsonFile.toString() + "\"\n"
          + e.getMessage());
      return false;
    }

    return this.setStatus(jsonObj);
  }

  /**
   * This function will save the current status (parameters) of the environment
   * as a JSON. The method returns the path to the file.
   * 
   * @param deleteOnExit Provide <code>true</code> when the file should be
   *                     deleted automatically, when the JVM terminates,
   *                     <code>false</code> otherwise
   * 
   * @return path to JSON file
   */
  public String saveStatus(boolean deleteOnExit) {

    try {

      File file = File.createTempFile("status", ".json");

      FileWriter writer = new FileWriter(file);
      writer.write(this.getStatus().toString());
      writer.close();

      if (deleteOnExit) {
        file.deleteOnExit();
      }

      return file.getAbsolutePath();

    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Get all performance identifiers
   * 
   * @param blacklistAnalyses analyses to be ignored during simulation
   * @return set of performance identifiers
   */
  public Set<String> getPerformanceIdentifiers(Set<String> blacklistAnalyses) {

    Set<String> retval = new HashSet<String>();

    Iterator<String> analysesIterator = this.jsonObject
        .getJSONObject(PERFORMANCES_ID).keys();

    Iterator<String> performanceIterator;

    String analysis;

    while (analysesIterator.hasNext()) {

      analysis = analysesIterator.next();

      if (!blacklistAnalyses.contains(analysis)) {

        performanceIterator = this.jsonObject.getJSONObject(PERFORMANCES_ID)
            .getJSONObject(analysis).keys();

        while (performanceIterator.hasNext()) {
          retval.add(performanceIterator.next());
        }
      }
    }
    return retval;
  }

  @Override
  public String toString() {

    int keyLen = 0;

    for (String key : this.getParameters().keySet()) {
      keyLen = Math.max(keyLen, key.length());
    }

    for (String key : this.getPerformanceIdentifiers()) {
      keyLen = Math.max(keyLen, key.length());
    }

    StringBuilder builder = new StringBuilder();

    builder.append("Parameters:");

    for (Entry<String, Double> entry : this.getParameterValues().entrySet()) {

      builder.append("\n\t" + entry.getKey());

      for (int i = 0; i < keyLen - entry.getKey().length(); i++) {
        builder.append(" ");
      }

      builder.append(" : " + entry.getValue());
    }

    builder.append("\n\nPerformances:");

    for (Entry<String, Double> entry : this.getPerformanceValues().entrySet()) {

      builder.append("\n\t" + entry.getKey());

      for (int i = 0; i < keyLen - entry.getKey().length(); i++) {
        builder.append(" ");
      }

      builder.append(" : " + entry.getValue());
    }

    return builder.toString();
  }

  @Override
  protected void finalize() {
    this.stop();
  }
}