package edlab.eda.ace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import edlab.eda.ardb.RealResultsDatabase;
import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.cadence.rc.spectre.SpectreFactory;
import edlab.eda.reader.nutmeg.NutmegPlot;
import edlab.eda.reader.nutmeg.NutmegRealPlot;

public class Nand4Environment extends AnalogCircuitEnvironment {

  public static final String DC0 = "dc0";

  public static final String DC1 = "dc1";

  public static final String DC2 = "dc2";

  public static final String DC3 = "dc3";

  protected Nand4Environment(SpectreFactory factory, JSONObject jsonObject,
      String netlist, File[] includeDirs) {
    super(factory, jsonObject, netlist, includeDirs);
  }

  /**
   * Get a new environment for a NAND-gate with 4 inputs
   * 
   * @param simDir      Directory where simulation results are stored
   * @param circuitDir  Directory that contains all information of the circuit.
   *                    The directory contains the files "input.scs" and
   *                    "properties.json".
   * @param includeDirs Array of include directories for simulation. These
   *                    include directories typically reference the model files
   *                    from the PDK.
   * 
   * @return object of {@link SingleEndedOpampEnvironment} when all parameters
   *         are valid, <code>null</code> otherwise
   */
  public static Nand4Environment get(String simDir, String circuitDir,
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

    String netlist;

    try {

      netlist = new String(Files.readAllBytes(netlistFile.toPath()));
    } catch (IOException e) {

      System.err.println("Cannot read file \"" + netlistFile.toString()
          + "\", \n" + e.getMessage());
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

    return new Nand4Environment(factory, jsonObj, netlist, includeDirFiles);
  }

  @Override
  public AnalogCircuitEnvironment simulate(Set<String> blacklistAnalyses) {

    this.performanceValues = new HashMap<String, Double>();

    List<NutmegPlot> plots;

    try {
      if (blacklistAnalyses == null || blacklistAnalyses.isEmpty()) {
        plots = this.session.simulate();
      } else {
        plots = this.session.simulate(blacklistAnalyses);
      }
    } catch (UnableToStartSession e) {
      e.printStackTrace();
      return null;
    }

    int resultIdentifier = 0;

    RealResultsDatabase rdb;

    if (!blacklistAnalyses.contains(DC0)) {

      rdb = RealResultsDatabase
          .buildResultDatabase((NutmegRealPlot) plots.get(resultIdentifier++));

      this.performanceValues.put("vs0",
          rdb.getRealWaveform("O").cross(1.65, 1).getValue());
    }

    if (!blacklistAnalyses.contains(DC1)) {

      rdb = RealResultsDatabase
          .buildResultDatabase((NutmegRealPlot) plots.get(resultIdentifier++));

      this.performanceValues.put("vs1",
          rdb.getRealWaveform("O").cross(1.65, 1).getValue());
    }

    if (!blacklistAnalyses.contains(DC2)) {

      rdb = RealResultsDatabase
          .buildResultDatabase((NutmegRealPlot) plots.get(resultIdentifier++));

      this.performanceValues.put("vs2",
          rdb.getRealWaveform("O").cross(1.65, 1).getValue());
    }

    if (!blacklistAnalyses.contains(DC3)) {

      rdb = RealResultsDatabase
          .buildResultDatabase((NutmegRealPlot) plots.get(resultIdentifier++));

      this.performanceValues.put("vs3",
          rdb.getRealWaveform("O").cross(1.65, 1).getValue());
    }

    return this;
  }
}
