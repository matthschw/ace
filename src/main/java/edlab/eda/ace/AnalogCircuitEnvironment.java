package edlab.eda.ace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import edlab.eda.cadence.rc.spectre.SpectreFactory;
import edlab.eda.cadence.rc.spectre.SpectreInteractiveSession;
import edlab.eda.cadence.rc.spectre.parallel.SpectreInteractiveParallelHandle;
import edlab.eda.cadence.rc.spectre.parallel.SpectreParallelPool;

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
  public static final String CORNERS_ID = "corners";
  public static final String NETLIST_ID = "netlist";
  public static final String NOMINAL_ID = "nominal";

  public static final String SCALE_ID = "scale";

  protected JSONObject jsonObject;

  protected Map<String, String> corners = new HashMap<>();
  protected Map<String, SpectreInteractiveParallelHandle> sessions = new HashMap<>();

  protected Map<String, Parameter> parameters;
  protected Map<String, Double> parameterValues;

  protected HashMap<String, HashMap<String, Double>> performanceValues;

  protected Map<String, String> errorMessage;
  private final SpectreFactory factory;
  private final File[] includeDirs;
  private final File dir;
  private BigDecimal scale;

  private final Thread parentThread = Thread.currentThread();

  protected String nomCorner = null;

  private int numOfRuns = 0;
  private int restartPeriod = Integer.MAX_VALUE;

  protected boolean verbose = true;
  protected boolean corrupted = false;

  private String name = "ace";

  protected AnalogCircuitEnvironment(final SpectreFactory factory,
      final JSONObject jsonObject, final File dir, final File[] includeDirs) {

    this.factory = factory;
    this.factory.setTimeout(1, TimeUnit.MINUTES);
    this.includeDirs = includeDirs;
    this.dir = dir;
    this.jsonObject = jsonObject;

    Iterator<String> iterator;
    String name;

    if (this.jsonObject.has(SCALE_ID)) {
      try {
        this.scale = this.jsonObject.getBigDecimal(SCALE_ID);
      } catch (Exception e) {
        this.scale = BigDecimal.ONE;
      }
    } else {
      this.scale = BigDecimal.ONE;
    }

    if (this.jsonObject.has(CORNERS_ID)) {

      final JSONObject cornersJsonObject = this.jsonObject
          .getJSONObject(CORNERS_ID);

      iterator = cornersJsonObject.keys();

      while (iterator.hasNext()) {

        name = iterator.next();

        this.corners.put(name,
            cornersJsonObject.getJSONObject(name).getString(NETLIST_ID));

        cornersJsonObject.getJSONObject(name).has(NOMINAL_ID);

        if (this.nomCorner == null) {
          this.nomCorner = name;
        }

        // use first entry in JSON as nominal corner
        if (cornersJsonObject.getJSONObject(name).has(NOMINAL_ID)) {
          if (cornersJsonObject.getJSONObject(name).getBoolean(NOMINAL_ID)) {
            this.nomCorner = name;
          }
        }
      }
    } else {
      this.corners.put(NOMINAL_DEFAULT, NETLIST_FILE_NAME);
      this.nomCorner = NOMINAL_DEFAULT;
    }

    this.parameterValues = new HashMap<>();
    this.performanceValues = new HashMap<>();
    this.parameters = new HashMap<>();

    this.errorMessage = new HashMap<>();

    final JSONObject parametersJsonObject = this.jsonObject
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

  /**
   * Enable verbose mode. When simulation throws errors, they will be displayed
   * in the console
   * 
   * @return this
   */
  public final AnalogCircuitEnvironment enableVerbose() {
    this.verbose = true;
    return this;
  }

  /**
   * Set the name of the environment
   * 
   * @param name Name
   * @return this
   */
  public final AnalogCircuitEnvironment setName(final String name) {
    this.name = name;
    return this;
  }

  /**
   * Disable verbose mode. When simulation throws errors, they will not
   * displayed in the console
   * 
   * @return this
   */
  public final AnalogCircuitEnvironment disableVerbose() {
    this.verbose = false;
    return this;
  }

  /**
   * Identify whether the run from last simulation is corrupted. Please enable
   * verbose mode to identify the reason.
   * 
   * @return <code>true</code> when the result is corrupted, <code>false</code>
   *         otherwise
   * 
   * @see AnalogCircuitEnvironment#enableVerbose()
   */
  public final boolean isCorrupted() {
    return this.corrupted;
  }

  /**
   * Get the technology scaling
   * 
   * @return scale
   */
  public final double getScale() {
    return this.scale.doubleValue();
  }

  /**
   * Allocate simulation sessions
   * 
   * @param corners set of corners to be simulated
   */
  private final void allocateSessions(final Set<String> corners) {

    SpectreInteractiveSession session;

    for (final String corner : corners) {

      if (!this.sessions.containsKey(corner)) {

        session = this.factory
            .createInteractiveSession(this.name + "." + corner);

        for (final File file : this.includeDirs) {
          try {
            session.addIncludeDirectory(file);
          } catch (final FileNotFoundException e) {
            e.printStackTrace();
          }
        }

        if (this.jsonObject.has(CORNERS_ID)) {
          try {
            session.addIncludeDirectory(this.dir);
          } catch (final FileNotFoundException e) {
            e.printStackTrace();
          }
        }

        try {
          session.setNetlist(new File(this.dir, this.corners.get(corner)));
        } catch (final IOException e) {
          e.printStackTrace();
        }

        session.setParentThread(this.parentThread);

        this.sessions.put(corner,
            new SpectreInteractiveParallelHandle(session));
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
  public  AnalogCircuitEnvironment simulate(final Set<String> blacklistAnalyses,
      Set<String> corners) {

    if ((corners == null) || corners.isEmpty()) {
      corners = new HashSet<>();
      corners.add(this.nomCorner);
    }

    this.allocateSessions(corners);

    final SpectreParallelPool pool = new SpectreParallelPool(
        this.corners.size());

    pool.setParentThread(this.parentThread);

    SpectreInteractiveParallelHandle session;

    Map<String, Object> values;

    for (final String corner : corners) {

      if (this.sessions.containsKey(corner)) {

        session = this.sessions.get(corner);

        pool.registerSession(session);
        session.setBlackListAnalyses(blacklistAnalyses);

        values = new HashMap<>();

        for (final Entry<String, Double> entry : this.parameterValues
            .entrySet()) {
          values.put(entry.getKey(), entry.getValue());
        }

        session.setValueAttributes(values);
      }
    }

    pool.run();

    this.numOfRuns++;

    if (this.numOfRuns >= this.restartPeriod) {

      this.numOfRuns = 0;

      for (SpectreInteractiveParallelHandle handle : this.sessions.values()) {
        handle.getSession().stop();
      }
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
  public AnalogCircuitEnvironment simulate(
      final Set<String> blacklistAnalyses) {

    final HashSet<String> corners = new HashSet<>();
    corners.add(this.nomCorner);

    return this.simulate(new HashSet<String>(), corners);
  }

  /**
   * Trigger a circuit simulation
   * 
   * @return <code>this</code>
   */
  public AnalogCircuitEnvironment simulate() {
    final HashSet<String> corners = new HashSet<>();
    corners.add(this.nomCorner);

    return this.simulate(new HashSet<String>(), corners);
  }

  /**
   * Get the restart period
   * 
   * @return restart period
   * @see #setRestartPeriod
   */
  public final int getRestartPeriod() {
    return this.restartPeriod;
  }

  /**
   * Specify the restart period of the environment.
   * 
   * @param restartPeriod
   * @return <code>this</code> when changing of the restart period was
   *         successfully, <code>null</code> otherwise
   */
  public final AnalogCircuitEnvironment setRestartPeriod(int restartPeriod) {
    if (restartPeriod > 0) {
      this.restartPeriod = restartPeriod;
      return this;
    } else {
      return null;
    }
  }

  /**
   * Stop the environment. When the method is not called, the environment is
   * stopped automatically when a timeout 15min with no action is exceeded.
   */
  public final void stop() {
    for (final SpectreInteractiveParallelHandle session : this.sessions
        .values()) {
      session.getSession().stop();
    }
  }

  /**
   * Clear the environment. When this method is called, all simulation runs are
   * stopped and the working directories are deleted
   * 
   * @return this
   */
  public final AnalogCircuitEnvironment clear() {

    for (final SpectreInteractiveParallelHandle session : this.sessions
        .values()) {

      session.getSession().stop();

      try {
        FileUtils
            .deleteDirectory(new File(session.getSession().getWorkingDir()));
      } catch (IOException e) {
      }
    }

    this.sessions = new HashMap<>();

    return this;
  }

  /**
   * Get a map of all parameters in the design. The key of the map corresponds
   * to the name of the parameter and the value is the corresponding parameter
   * handle of class {@link Parameter}.
   * 
   * @return map of parameters
   */
  public final Map<String, Parameter> getParameters() {
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
  public final Map<String, Parameter> getSizingParameters() {

    final Map<String, Parameter> retval = new HashMap<>();

    for (final Entry<String, Parameter> entry : this.parameters.entrySet()) {
      retval.put(entry.getKey(), entry.getValue());
    }

    return retval;
  }

  /**
   * Get the parameter values that are currently set in the circuit
   * 
   * @return map of parameter names and parameter values
   */
  public final Map<String, Double> getParameterValues() {
    return this.parameterValues;
  }

  /**
   * Get the name of the nominal corner
   * 
   * @return name of nominal corner
   */
  public final String getNominalCorner() {
    return this.nomCorner;
  }

  /**
   * Get a set of all corners
   * 
   * @return set of corners
   */
  public final Set<String> getCorners() {
    return this.corners.keySet();
  }

  /**
   * Get the performance values from the last simulation as a map from nominal
   * corner
   * 
   * @return map of performances
   */
  public final Map<String, Double> getPerformanceValues() {
    return this.performanceValues.get(this.nomCorner);
  }

  /**
   * Get the performance values from the last simulation as a map for a corner
   * 
   * @param corner name of corner
   * 
   * @return map of performances
   */
  public final Map<String, Double> getPerformanceValues(final String corner) {
    return this.performanceValues.get(corner);
  }

  /**
   * Get the performance values for all corners. The method returns a map of
   * maps. The key of the outer map corresponds to the name of the corner, the
   * key of the inner name
   * 
   * @return map of maps of performances
   */
  public final Map<String, HashMap<String, Double>> getAllPerformanceValues() {
    return this.performanceValues;
  }

  /**
   * Set a parameter in the circuit to a specific value
   * 
   * @param name  Name of the parameter
   * @param value New value of the parameter
   * @return true
   */
  public final boolean set(final String name, double value) {

    if (this.parameters.containsKey(name)) {

      value = this.parameters.get(name).getValidValue(value);

      this.parameterValues.put(name, value);

      return true;

    } else {

      System.err.println("\"" + name + "\" is not a parameter");

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
  public final boolean set(final Map<String, Double> values) {

    boolean retval = true;

    for (final String name : values.keySet()) {

      if (!this.set(name, values.get(name))) {
        retval = false;
      }
    }

    return retval;
  }

  /**
   * Get a map of random sizing parameters in the design. The key of the map
   * corresponds to the name of the parameter and a random value.
   * 
   * @return map of parameters
   * @see Parameter#isSizingParameter()
   */
  public final Map<String, Double> getRandomSizingParameters() {

    final Map<String, Double> retval = new HashMap<>();

    for (final Entry<String, Parameter> entry : this.parameters.entrySet()) {

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
  public final Map<String, Double> getInitialSizingParameters() {

    final Map<String, Double> retval = new HashMap<>();

    for (final Entry<String, Parameter> entry : this.parameters.entrySet()) {

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
  public final Set<String> getAnalyses() {
    final Set<String> retval = new HashSet<>();

    final Iterator<String> analysesIterator = this.jsonObject
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
  public final Set<String> getPerformanceIdentifiers() {
    return this.getPerformanceIdentifiers(new HashSet<String>());
  }

  /**
   * Get the status of the environment (all parameters) as a JSON object
   * 
   * @return JSON object
   */
  public final JSONObject getStatus() {

    final JSONObject jsonObj = new JSONObject();

    for (final Entry<String, Double> entry : this.getParameterValues()
        .entrySet()) {
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
  public final boolean setStatus(final JSONObject jsonObj) {

    for (final String key : this.parameters.keySet()) {

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
  public final boolean setStatus(final String file) {

    final File jsonFile = new File(file);

    if (!(jsonFile.exists() && jsonFile.canRead())) {

      System.err.println("Cannot read file \"" + jsonFile.toString() + "\"");

      return false;
    }

    JSONObject jsonObj;

    try {

      jsonObj = new JSONObject(
          new String(Files.readAllBytes(jsonFile.toPath())));
    } catch (final Exception e) {
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
  public final String saveStatus(final boolean deleteOnExit) {

    try {

      final File file = File.createTempFile("status", ".json");

      final FileWriter writer = new FileWriter(file);
      writer.write(this.getStatus().toString());
      writer.close();

      if (deleteOnExit) {
        file.deleteOnExit();
      }

      return file.getAbsolutePath();

    } catch (final IOException e) {
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
  public final Set<String> getPerformanceIdentifiers(
      final Set<String> blacklistAnalyses) {

    final Set<String> retval = new HashSet<>();

    final Iterator<String> analysesIterator = this.jsonObject
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

    for (final String key : this.getParameters().keySet()) {
      keyLen = Math.max(keyLen, key.length());
    }

    for (final String key : this.getPerformanceIdentifiers()) {
      keyLen = Math.max(keyLen, key.length());
    }

    final StringBuilder builder = new StringBuilder();

    builder.append("Parameters:");

    for (final Entry<String, Double> entry : this.getParameterValues()
        .entrySet()) {

      builder.append("\n\t" + entry.getKey());

      for (int i = 0; i < (keyLen - entry.getKey().length()); i++) {
        builder.append(" ");
      }

      builder.append(" : " + entry.getValue());
    }

    for (final String corner : this.performanceValues.keySet()) {
      builder.append("\n\n" + corner + " :");
      for (final Entry<String, Double> entry : this.getPerformanceValues(corner)
          .entrySet()) {

        builder.append("\n\t" + entry.getKey());

        for (int i = 0; i < (keyLen - entry.getKey().length()); i++) {
          builder.append(" ");
        }

        builder.append(" : " + entry.getValue());
      }
    }

    return builder.toString();
  }

  @Override
  protected void finalize() {
    this.stop();
  }
}