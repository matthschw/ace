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
import edlab.eda.cadence.rc.spectre.SpectreFactory;
import edlab.eda.reader.nutmeg.NutmegPlot;
import edlab.eda.reader.nutmeg.NutmegRealPlot;

/**
 * Environment for characterization of a NAND gate with 4 inputs.
 */
public final class Nand4Environment extends AnalogCircuitEnvironment {

  public static final String DC0 = "dc0";

  public static final String DC1 = "dc1";

  public static final String DC2 = "dc2";

  public static final String DC3 = "dc3";

  protected Nand4Environment(final SpectreFactory factory, final JSONObject jsonObject,
      final File dir, final File[] includeDirs) {
    super(factory, jsonObject, dir, includeDirs);
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
   * @return object of {@link Nand4Environment} when all parameters are valid,
   *         <code>null</code> otherwise
   */
  public static Nand4Environment get(final String simDir, final String circuitDir,
      final String[] includeDirs) {

    final File simDirFile = new File(simDir);

    if (!(simDirFile.exists() && simDirFile.isDirectory()
        && simDirFile.canRead())) {
      System.err
          .println("Cannot write simulation results to \"" + simDir + "\"");
      return null;
    }

    final SpectreFactory factory = SpectreFactory.getSpectreFactory(simDirFile);

    if (factory == null) {
      System.err.println("Unable to access simulator spectre");
      return null;
    }

    factory.setTimeout(10, TimeUnit.SECONDS);

    final File circuitDirFile = new File(circuitDir);

    if (!(circuitDirFile.exists() && circuitDirFile.isDirectory()
        && circuitDirFile.canWrite())) {
      System.err.println("\"" + circuitDirFile + "\" is not a directory");
      return null;
    }

    final File jsonFile = new File(circuitDirFile,
        AnalogCircuitEnvironment.JSON_FILE_NAME);

    if (!(jsonFile.exists() && jsonFile.canRead())) {

      System.err.println("Cannot read file \"" + jsonFile.toString() + "\"");

      return null;
    }

    JSONObject jsonObj;
    


    try {

      jsonObj = new JSONObject(
          new String(Files.readAllBytes(jsonFile.toPath())));
    } catch (final Exception e) {

      System.err.println("Cannot read JSON \"" + jsonFile.toString() + "\"\n"
          + e.getMessage());
      return null;
    }

    final File[] includeDirFiles = new File[includeDirs.length];
    File includeDir;

    for (int i = 0; i < includeDirFiles.length; i++) {

      includeDir = new File(includeDirs[i]);

      if (!(includeDir.exists() && includeDir.isDirectory())) {

        System.err.println("\"" + includeDirs[i] + "\" is not a directory");

        return null;
      }

      includeDirFiles[i] = includeDir;
    }

    return new Nand4Environment(factory, jsonObj, circuitDirFile,
        includeDirFiles);
  }

  @Override
  public AnalogCircuitEnvironment simulate(final Set<String> blacklistAnalyses,
      final Set<String> corners) {

    super.simulate(blacklistAnalyses, corners);

    this.performanceValues = new HashMap<>();

    List<NutmegPlot> plots;
    int resultIdentifier;
    HashMap<String, Double> performanceValues;

    for (final String corner : corners) {

      resultIdentifier = 0;
      plots = this.sessions.get(corner).getPlots();
      performanceValues = new HashMap<>();

      RealResultsDatabase rdb;

      if (!blacklistAnalyses.contains(DC0)) {

        rdb = RealResultsDatabase.buildResultDatabase(
            (NutmegRealPlot) plots.get(resultIdentifier++));

        performanceValues.put("vs0",
            rdb.getRealWaveform("O").cross(1.65, 1).getValue());
      }

      if (!blacklistAnalyses.contains(DC1)) {

        rdb = RealResultsDatabase.buildResultDatabase(
            (NutmegRealPlot) plots.get(resultIdentifier++));

        performanceValues.put("vs1",
            rdb.getRealWaveform("O").cross(1.65, 1).getValue());
      }

      if (!blacklistAnalyses.contains(DC2)) {

        rdb = RealResultsDatabase.buildResultDatabase(
            (NutmegRealPlot) plots.get(resultIdentifier++));

        performanceValues.put("vs2",
            rdb.getRealWaveform("O").cross(1.65, 1).getValue());
      }

      if (!blacklistAnalyses.contains(DC3)) {

        rdb = RealResultsDatabase.buildResultDatabase(
            (NutmegRealPlot) plots.get(resultIdentifier++));

        performanceValues.put("vs3",
            rdb.getRealWaveform("O").cross(1.65, 1).getValue());
      }
      
      this.performanceValues.put(corner, performanceValues);
    }

    return this;
  }

  @Override
  public AnalogCircuitEnvironment simulate(final Set<String> blacklistAnalyses) {
    final HashSet<String> corners = new HashSet<>();
    corners.add(this.nomCorner);
    return this.simulate(blacklistAnalyses, corners);
  }

  @Override
  public AnalogCircuitEnvironment simulate() {
    final HashSet<String> corners = new HashSet<>();
    corners.add(this.nomCorner);
    return this.simulate(new HashSet<String>(), corners);
  }
}
