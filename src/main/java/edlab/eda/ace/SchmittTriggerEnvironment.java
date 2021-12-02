package edlab.eda.ace;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import edlab.eda.ardb.RealResultsDatabase;
import edlab.eda.ardb.RealWaveform;
import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.cadence.rc.spectre.SpectreFactory;
import edlab.eda.reader.nutmeg.NutmegPlot;
import edlab.eda.reader.nutmeg.NutmegRealPlot;

/**
 * Environment for characterization of a Schmitt-Trigger.
 */
public class SchmittTriggerEnvironment extends AnalogCircuitEnvironment {

  public static final String TRAN = "tran";
  public static final double T1 = 1.0;
  public static final double T2 = 100.0e-12;

  protected SchmittTriggerEnvironment(SpectreFactory factory,
      JSONObject jsonObject, File dir, File[] includeDirs) {
    super(factory, jsonObject, dir, includeDirs);
  }

  /**
   * Get a new environment for a Schmitt-Trigger
   * 
   * @param simDir      Directory where simulation results are stored
   * @param circuitDir  Directory that contains all information of the circuit.
   *                    The directory contains the files "input.scs" and
   *                    "properties.json".
   * @param includeDirs Array of include directories for simulation. These
   *                    include directories typically reference the model files
   *                    from the PDK.
   * 
   * @return object of {@link SchmittTriggerEnvironment} when all parameters are
   *         valid, <code>null</code> otherwise
   */
  public static SchmittTriggerEnvironment get(String simDir, String circuitDir,
      String[] includeDirs) {

    File simDirFile = new File(simDir);

    if (!(simDirFile.exists() && simDirFile.isDirectory()
        && simDirFile.canRead())) {
      System.err
          .println("Cannot write simulation results to \"" + simDir + "\"");
      return null;
    }

    SpectreFactory factory = SpectreFactory.getSpectreFactory(simDirFile);

    if (factory == null) {
      System.err.println("Unable to access simulator spectre");
      return null;
    }

    factory.setTimeout(10, TimeUnit.SECONDS);

    File circuitDirFile = new File(circuitDir);

    if (!(circuitDirFile.exists() && circuitDirFile.isDirectory()
        && circuitDirFile.canWrite())) {
      System.err.println("\"" + circuitDirFile + "\" is not a directory");
      return null;
    }

    File jsonFile = new File(circuitDirFile,
        AnalogCircuitEnvironment.JSON_FILE_NAME);

    if (!(jsonFile.exists() && jsonFile.canRead())) {

      System.err.println("Cannot read file \"" + jsonFile.toString() + "\"");

      return null;
    }

    JSONObject jsonObj;

    try {

      jsonObj = new JSONObject(
          new String(Files.readAllBytes(jsonFile.toPath())));
    } catch (Exception e) {

      System.err.println("Cannot read JSON \"" + jsonFile.toString() + "\"\n"
          + e.getMessage());
      return null;
    }

    File netlistFile = new File(circuitDirFile,
        AnalogCircuitEnvironment.NETLIST_FILE_NAME);

    if (!(netlistFile.exists() && netlistFile.canRead())) {

      System.err
          .println("Cannot read netlist \"" + netlistFile.toString() + "\"");
      return null;
    }

    File[] includeDirFiles = new File[includeDirs.length];
    File includeDir;

    for (int i = 0; i < includeDirFiles.length; i++) {

      includeDir = new File(includeDirs[i]);

      if (!(includeDir.exists() && includeDir.isDirectory())) {

        System.err.println("\"" + includeDirs[i] + "\" is not a directory");

        return null;
      }

      includeDirFiles[i] = includeDir;
    }

    return new SchmittTriggerEnvironment(factory, jsonObj, circuitDirFile,
        includeDirFiles);
  }

  @Override
  public AnalogCircuitEnvironment simulate(Set<String> blacklistAnalyses,
      Set<String> corners) {

    super.simulate(blacklistAnalyses, corners);

    this.performanceValues = new HashMap<String, HashMap<String, Double>>();

    List<NutmegPlot> plots;
    int resultIdentifier;
    HashMap<String, Double> performanceValues;
    
    double vdd = Double.NaN;

    try {
      vdd = this.sessions.get(corners.iterator().next())
          .getSession().getNumericValueAttribute("vdd").doubleValue();
    } catch (UnableToStartSession e) {
    }

    for (String corner : corners) {
      
      resultIdentifier = 0;
      plots = this.sessions.get(corner).getPlots();
      performanceValues = new HashMap<String, Double>();
      
      RealResultsDatabase rdb;

      if (!blacklistAnalyses.contains(TRAN)) {

        rdb = RealResultsDatabase.buildResultDatabase(
            (NutmegRealPlot) plots.get(resultIdentifier++));

        RealWaveform i = rdb.getRealWaveform("I");
        RealWaveform o = rdb.getRealWaveform("O");

        performanceValues.put("v_ih",
            i.getValue(o.clip(0, T1).cross(vdd / 2, 1)).getValue());
        performanceValues.put("v_il",
            i.getValue(o.clip(T1, 2 * T1).cross(vdd / 2, 1)).getValue());

        performanceValues.put("t_phl",
            o.clip(3 * T1, 4 * T1).cross(vdd / 2, 1).getValue()
                - i.clip(3 * T1, 4 * T1).cross(vdd / 2, 1).getValue());

       performanceValues.put("t_plh",
            o.clip(4 * T1 + T2, 5 * T1 + T2).cross(vdd / 2, 1).getValue() - i
                .clip(4 * T1 + T2, 5 * T1 + T2).cross(vdd / 2, 1).getValue());
      }
      
      this.performanceValues.put(corner, performanceValues);
    }

    return this;
  }

  @Override
  public AnalogCircuitEnvironment simulate(Set<String> blacklistAnalyses) {
    HashSet<String> corners = new HashSet<String>();
    corners.add(this.nomCorner);
    return this.simulate(blacklistAnalyses, corners);
  }

  @Override
  public AnalogCircuitEnvironment simulate() {
    HashSet<String> corners = new HashSet<String>();
    corners.add(this.nomCorner);
    return this.simulate(new HashSet<String>(), corners);
  }
}
