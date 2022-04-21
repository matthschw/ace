package edlab.eda.ace;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import edlab.eda.ardb.ComplexResultsDatabase;
import edlab.eda.ardb.ComplexWaveform;
import edlab.eda.ardb.RealResultsDatabase;
import edlab.eda.ardb.RealValue;
import edlab.eda.ardb.RealWaveform;
import edlab.eda.cadence.rc.spectre.SpectreFactory;
import edlab.eda.cadence.rc.spectre.SpectreSession;
import edlab.eda.cadence.rc.spectre.UnableToStartSpectreSession;
import edlab.eda.reader.nutmeg.NutmegComplexPlot;
import edlab.eda.reader.nutmeg.NutmegPlot;
import edlab.eda.reader.nutmeg.NutmegRealPlot;

/**
 * Environment for characterization of a single-ended operational amplifier.
 */
public final class SingleEndedOpampEnvironment
    extends AnalogCircuitEnvironment {

  protected double dev = 1e-4;
  protected double settling_percentage = 5;

  /**
   * analysis identifier in the netlist of the DC operating-point analysis
   */
  public static final String DCOP_ANALYSIS_ID = "dcop";
  /**
   * analysis identifier in the netlist of the dcmatch analysis
   */
  public static final String DCMATCH_ANALYSIS_ID = "dcmatch";
  /**
   * analysis identifier in the netlist of the stability analysis
   */
  public static final String STB_ANALYSIS_ID = "stb";
  /**
   * analysis identifier in the netlist of the transient analysis
   */
  public static final String TRAN_ANALYSIS_ID = "tran";
  /**
   * analysis identifier in the netlist of the noise analysis
   */
  public static final String NOISE_ANALYSIS_ID = "noise";
  /**
   * analysis identifier in the netlist of the dc1 analysis
   */
  public static final String DC1_ANALYSIS_ID = "dc1";
  /**
   * analysis identifier in the netlist of the transfer-function analysis
   */
  public static final String XF_ANALYSIS_ID = "xf";
  /**
   * analysis identifier in the netlist of the ac analysis
   */
  public static final String AC_ANALYSIS_ID = "ac";
  /**
   * analysis identifier in the netlist of the dc3 analysis
   */
  public static final String DC3_ANALYSIS_ID = "dc3";
  /**
   * analysis identifier in the netlist of the dc4 analysis
   */
  public static final String DC4_ANALYSIS_ID = "dc4";

  private SingleEndedOpampEnvironment(final SpectreFactory factory,
      final JSONObject jsonObject, final File dir, final File[] includeDirs) {
    super(factory, jsonObject, dir, includeDirs);
  }

  /**
   * Get a new environment for a single-ended operational amplifier
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
  public static SingleEndedOpampEnvironment get(final String simDir,
      final String circuitDir, final String[] includeDirs) {

    final File simDirFile = new File(simDir);

    if (!(simDirFile.exists() && simDirFile.isDirectory()
        && simDirFile.canRead() && simDirFile.canWrite())) {

      System.err
          .println("Cannot write simulation results to \"" + simDir + "\"");

      return null;
    }

    final SpectreFactory factory = SpectreFactory.getSpectreFactory(simDirFile);

    if (factory == null) {
      System.err.println("Unable to access simulator spectre");
      return null;
    }

    factory.setWatchogTimeout(10, TimeUnit.SECONDS);

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

    SingleEndedOpampEnvironment env = new SingleEndedOpampEnvironment(factory,
        jsonObj, circuitDirFile, includeDirFiles);

    env.setName(circuitDirFile.getName());
    
    return env;
  }

  private void identifiedCorruptedResults(String analysis, String corner) {

    if (this.verbose) {
      System.err
          .println("Results from analysis \"" + analysis + "\" and corner \""
              + corner + "\" are not available. Please check the logfile("
              + SpectreSession.LOG_FILENAME + ") in \""
              + this.sessions.get(corner).getSession().getWorkingDir() + "\"");
    }

    this.corrupted = true;
  }

  @Override
  public AnalogCircuitEnvironment simulate(final Set<String> blacklistAnalyses,
      final Set<String> corners) {

    this.corrupted = false;

    super.simulate(blacklistAnalyses, corners);

    this.performanceValues = new HashMap<>();

    HashMap<String, Double> performanceValues;

    Map<String, NutmegPlot> plotsMap;

    JSONObject performances;

    Iterator<String> iterator;
    String key;
    JSONObject performance;
    String reference;

    double area = Double.NaN;

    try {
      area = this.sessions.get(corners.iterator().next()).getSession()
          .getNumericValueAttribute("A").doubleValue();

    } catch (UnableToStartSpectreSession e) {

      e.printStackTrace();

      System.err.print(e.readLogfile());
    }

    for (final String corner : corners) {

      plotsMap = NutmegPlot.getPlotMap(this.sessions.get(corner).getPlots());

      performanceValues = new HashMap<>();

      // Extract the result from "dcop" analysis
      performances = this.jsonObject.getJSONObject(PERFORMANCES_ID)
          .getJSONObject(DCOP_ANALYSIS_ID);
      iterator = performances.keys();

      if (!blacklistAnalyses.contains(DCOP_ANALYSIS_ID)) {

        if (plotsMap.containsKey(DCOP_ANALYSIS_ID)) {

          final RealResultsDatabase dcopResults = RealResultsDatabase
              .buildResultDatabase(
                  (NutmegRealPlot) plotsMap.get(DCOP_ANALYSIS_ID));

          while (iterator.hasNext()) {

            key = iterator.next();
            performance = performances.getJSONObject(key);

            if (performance.has(REFERENCE_ID)) {

              reference = performance.getString(REFERENCE_ID);

              if (dcopResults.isMember(reference)) {
                performanceValues.put(key,
                    dcopResults.getRealValue(reference).getValue());
              } else {

                if (this.verbose) {
                  this.corrupted = true;
                  System.err
                      .println("\"" + key + "\" not available for analysis \""
                          + DCOP_ANALYSIS_ID + "\"");
                }
              }
            }
          }

          performanceValues.put("A", area);

        } else {
          this.identifiedCorruptedResults(DCOP_ANALYSIS_ID, corner);
        }
      }

      // Extract the result from "dcmatch" analysis
      performances = this.jsonObject.getJSONObject(PERFORMANCES_ID)
          .getJSONObject(DCMATCH_ANALYSIS_ID);
      iterator = performances.keys();

      if (!blacklistAnalyses.contains(DCMATCH_ANALYSIS_ID)) {

        if (plotsMap.containsKey(DCMATCH_ANALYSIS_ID)) {

          final RealResultsDatabase dcmatchResults = RealResultsDatabase
              .buildResultDatabase(
                  (NutmegRealPlot) plotsMap.get(DCMATCH_ANALYSIS_ID));

          while (iterator.hasNext()) {

            key = iterator.next();
            performance = performances.getJSONObject(key);

            reference = performance.getString(REFERENCE_ID);

            if (dcmatchResults.isMember(reference)) {
              performanceValues.put(key,
                  dcmatchResults.getRealValue(reference).getValue());
            } else {
              // System.out.println("\"" + key + "\" not available in
              // database");
            }
          }

        } else {
          this.identifiedCorruptedResults(DCMATCH_ANALYSIS_ID, corner);
        }
      }

      // Extract the result from "stb" analysis
      if (!blacklistAnalyses.contains(STB_ANALYSIS_ID)) {

        if (plotsMap.containsKey(STB_ANALYSIS_ID)) {

          final NutmegComplexPlot plot = (NutmegComplexPlot) plotsMap
              .get(STB_ANALYSIS_ID);

          final ComplexResultsDatabase stb = ComplexResultsDatabase
              .buildResultDatabase(plot);

          final ComplexWaveform loopGain = stb.getComplexWaveform("loopGain");

          // System.err.println(loopGain);

          // waves.put("loopGain", loopGain);

          final RealWaveform loopGainAbs = loopGain.abs().db20();

          final RealWaveform loopGainPhase = loopGain.phaseDeg();

          // waves.put("loopGainAbs", loopGainAbs);
          // waves.put("loopGainPhase", loopGainPhase);

          final RealValue a0 = loopGainAbs.getValue(loopGainAbs.xmin());
          final RealValue ugbw = loopGainAbs.cross(0, 1);

          final RealValue pm = loopGainPhase.getValue(ugbw.getValue());

          final RealValue cof = loopGainPhase.cross(0, 1);

          final RealValue gm = loopGainAbs.getValue(cof.getValue());

          performanceValues.put("a_0", a0.getValue());
          performanceValues.put("ugbw", ugbw.getValue());
          performanceValues.put("cof", cof.getValue());
          performanceValues.put("pm", pm.getValue());
          performanceValues.put("gm", gm.getValue());
        } else {
          this.identifiedCorruptedResults(STB_ANALYSIS_ID, corner);
        }
      }

      // Extract the result from "tran" analysis
      if (!blacklistAnalyses.contains(TRAN_ANALYSIS_ID)) {

        if (plotsMap.containsKey(TRAN_ANALYSIS_ID)) {

          final RealResultsDatabase tran = RealResultsDatabase
              .buildResultDatabase(
                  (NutmegRealPlot) plotsMap.get(TRAN_ANALYSIS_ID));

          final RealWaveform out = tran.getRealWaveform("OUT");

          final RealWaveform rising = out.clip(100e-9, 50e-6);
          final RealWaveform falling = out.clip(50.1e-6, 99.9e-6);

          final double lower = (0.1 * this.getParameterValues().get("vs"))
              - (this.getParameterValues().get("vs") / 2);
          final double upper = (0.9 * this.getParameterValues().get("vs"))
              - (this.getParameterValues().get("vs") / 2);

          RealValue point1 = rising.cross(lower, 1);
          RealValue point2 = rising.cross(upper, 1);

          performanceValues.put("sr_r",
              (upper - lower) / (point2.getValue() - point1.getValue()));

          if (performanceValues.get("sr_r") == Double.NaN) {
            performanceValues.put("sr_r", Double.MIN_VALUE);
          }

          point1 = falling.cross(upper, 1);
          point2 = falling.cross(lower, 1);

          performanceValues.put("sr_f",
              (lower - upper) / (point2.getValue() - point1.getValue()));

          if (performanceValues.get("sr_f") == Double.NaN) {
            performanceValues.put("sr_f", Double.MAX_VALUE);
          }

          performanceValues.put("overshoot_r", (100
              * (rising.ymax().getValue() - out.getValue(50e-6).getValue()))
              / (out.getValue(50e-6).getValue()
                  - out.getValue(100e-9).getValue()));

          if (performanceValues.get("overshoot_r") == Double.NaN) {
            performanceValues.put("overshoot_r", Double.MAX_VALUE);
          }

          performanceValues.put("overshoot_f", (100
              * (falling.ymin().getValue() - out.getValue(90e-6).getValue()))
              / (out.getValue(90e-6).getValue()
                  - out.getValue(50e-6).getValue()));

          if (performanceValues.get("overshoot_f") == Double.NaN) {
            performanceValues.put("overshoot_f", Double.MAX_VALUE);
          }
        } else {
          this.identifiedCorruptedResults(TRAN_ANALYSIS_ID, corner);
        }
      }

      // Extract the result from "noise" analysis
      if (!blacklistAnalyses.contains(NOISE_ANALYSIS_ID)) {

        if (plotsMap.containsKey(NOISE_ANALYSIS_ID)) {
          final RealResultsDatabase noise = RealResultsDatabase
              .buildResultDatabase(
                  (NutmegRealPlot) plotsMap.get(NOISE_ANALYSIS_ID));

          final RealWaveform out = noise.getRealWaveform("out");

          performanceValues.put("vn_1Hz", out.getValue(1).getValue());
          performanceValues.put("vn_10Hz", out.getValue(10).getValue());
          performanceValues.put("vn_100Hz", out.getValue(1e2).getValue());
          performanceValues.put("vn_1kHz", out.getValue(1e3).getValue());
          performanceValues.put("vn_10kHz", out.getValue(1e4).getValue());
          performanceValues.put("vn_100kHz", out.getValue(1e5).getValue());

        } else {
          this.identifiedCorruptedResults(NOISE_ANALYSIS_ID, corner);
        }
      }

      // Extract the result from "dc1" analysis
      if (!blacklistAnalyses.contains(DC1_ANALYSIS_ID)) {

        if (plotsMap.containsKey(DC1_ANALYSIS_ID)) {

          final RealResultsDatabase outswing = RealResultsDatabase
              .buildResultDatabase(
                  (NutmegRealPlot) plotsMap.get(DC1_ANALYSIS_ID));

          RealWaveform out = outswing.getRealWaveform("OUT");
          final RealWaveform out_ideal = outswing.getRealWaveform("OUT_IDEAL");
          out = out.subtract(out.getValue(0));

          RealWaveform rel_dev = out.subtract(out_ideal);

          rel_dev = rel_dev.abs().divide(this.getParameterValues().get("vsup"));

          final RealWaveform rel_dev_lower = rel_dev
              .clip(rel_dev.xmin().getValue(), 0);
          final RealWaveform rel_dev_upper = rel_dev.clip(0,
              rel_dev.xmax().getValue());

          final RealValue vil = rel_dev_lower.cross(this.dev, 1);
          final RealValue vih = rel_dev_upper.cross(this.dev, 1);

          final RealValue voh = out.getValue(vih);
          final RealValue vol = out.getValue(vil);

          performanceValues.put("v_ol",
              vol.getValue() + (this.getParameterValues().get("vsup") / 2));
          performanceValues.put("v_oh",
              voh.getValue() + (this.getParameterValues().get("vsup") / 2));

        } else {
          this.identifiedCorruptedResults(DC1_ANALYSIS_ID, corner);
        }

      } else {
        this.performanceValues.remove("v_ol");
        this.performanceValues.remove("v_oh");
      }

      // Extract the result from "xf" analysis
      if (!blacklistAnalyses.contains(XF_ANALYSIS_ID)) {

        if (plotsMap.containsKey(XF_ANALYSIS_ID)) {
          final ComplexResultsDatabase tf = ComplexResultsDatabase
              .buildResultDatabase(
                  (NutmegComplexPlot) plotsMap.get(XF_ANALYSIS_ID));

          final ComplexWaveform vsupp = tf.getComplexWaveform("VSUPP");
          final ComplexWaveform vsupn = tf.getComplexWaveform("VSUPN");
          final ComplexWaveform vid = tf.getComplexWaveform("VID");
          final ComplexWaveform vicm = tf.getComplexWaveform("VICM");

          final RealWaveform vsuppAbs = vsupp.abs().db20();
          final RealWaveform vsupnAbs = vsupn.abs().db20();
          final RealWaveform vidAbs = vid.abs().db20();
          final RealWaveform vicmAbs = vicm.abs().db20();

          final RealWaveform psrr_p = vidAbs.subtract(vsuppAbs);
          final RealWaveform psrr_n = vidAbs.subtract(vsupnAbs);
          final RealWaveform cmrr = vidAbs.subtract(vicmAbs);

          performanceValues.put("psrr_p",
              psrr_p.getValue(psrr_p.xmin()).getValue());
          performanceValues.put("psrr_n",
              psrr_n.getValue(psrr_n.xmin()).getValue());
          performanceValues.put("cmrr", cmrr.getValue(cmrr.xmin()).getValue());

        } else {
          this.identifiedCorruptedResults(XF_ANALYSIS_ID, corner);
        }
      }

      // Extract the result from "ac" analysis
      if (!blacklistAnalyses.contains(AC_ANALYSIS_ID)) {

        if (plotsMap.containsKey(AC_ANALYSIS_ID)) {

          final ComplexResultsDatabase inswing = ComplexResultsDatabase
              .buildResultDatabase(
                  (NutmegComplexPlot) plotsMap.get(AC_ANALYSIS_ID));

          final RealWaveform out = inswing.getComplexWaveform("OUT").abs()
              .db20();

          final RealWaveform rel_dev_lower = out.clip(out.xmin().getValue(), 0);
          final RealWaveform rel_dev_upper = out.clip(0, out.xmax().getValue());

          final RealValue amp = out.getValue(0);
          final RealValue vil = rel_dev_lower.cross(amp.getValue() - 3, 1);
          final RealValue vih = rel_dev_upper.cross(amp.getValue() - 3, 1);

          performanceValues.put("v_il",
              vil.getValue() + (this.getParameterValues().get("vsup") / 2));
          performanceValues.put("v_ih",
              vih.getValue() + (this.getParameterValues().get("vsup") / 2));
        } else {
          this.identifiedCorruptedResults(AC_ANALYSIS_ID, corner);
        }

      }

      // Extract the result from "dc3" analysis
      if (!blacklistAnalyses.contains(DC3_ANALYSIS_ID)) {

        if (plotsMap.containsKey(DC3_ANALYSIS_ID)) {

          final RealResultsDatabase outshortl = RealResultsDatabase
              .buildResultDatabase(
                  (NutmegRealPlot) plotsMap.get(DC3_ANALYSIS_ID));

          performanceValues.put("i_out_min",
              outshortl.getRealValue("DUT:O").getValue());

        } else {
          this.identifiedCorruptedResults(DC3_ANALYSIS_ID, corner);
        }
      }

      // Extract the result from "dc4" analysis
      if (!blacklistAnalyses.contains(DC4_ANALYSIS_ID)) {

        if (plotsMap.containsKey(DC4_ANALYSIS_ID)) {
          final RealResultsDatabase outshorth = RealResultsDatabase
              .buildResultDatabase(
                  (NutmegRealPlot) plotsMap.get(DC4_ANALYSIS_ID));

          performanceValues.put("i_out_max",
              outshorth.getRealValue("DUT:O").getValue());
        } else {
          this.identifiedCorruptedResults(DC4_ANALYSIS_ID, corner);
        }
      }

      this.performanceValues.put(corner, performanceValues);
    }

    return this;
  }

  @Override
  public AnalogCircuitEnvironment simulate(
      final Set<String> blacklistAnalyses) {
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