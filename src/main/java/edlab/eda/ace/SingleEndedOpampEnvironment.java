package edlab.eda.ace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;

import edlab.eda.ardb.ComplexResultsDatabase;
import edlab.eda.ardb.ComplexWaveform;
import edlab.eda.ardb.RealResultsDatabase;
import edlab.eda.ardb.RealValue;
import edlab.eda.ardb.RealWaveform;
import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.cadence.rc.spectre.SpectreFactory;
import edlab.eda.cadence.rc.spectre.SpectreSession;
import edlab.eda.reader.nutmeg.NutmegPlot;
import edlab.eda.reader.nutmeg.NutmegRealPlot;
import edlab.eda.reader.nutmeg.NutmegComplexPlot;

/**
 * Environment for characterization of a single-ended operational amplifier.
 */
public class SingleEndedOpampEnvironment extends AnalogCircuitEnvironment {

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

  private SingleEndedOpampEnvironment(SpectreFactory factory,
      JSONObject jsonObject, File dir, File[] includeDirs) {
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
  public static SingleEndedOpampEnvironment get(String simDir,
      String circuitDir, String[] includeDirs) {

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

    return new SingleEndedOpampEnvironment(factory, jsonObj, circuitDirFile,
        includeDirFiles);
  }
  
  private void processResults() {
    
    this.performanceValues = new HashMap<String, HashMap<String,Double>>();
    
    for (Entry<String, SpectreSession> entry : this.sessions.entrySet()) {
      
      List<NutmegPlot> plots;
      
    }
    
    
  }

  @Override
  public SingleEndedOpampEnvironment simulate(Set<String> blacklistAnalyses) {
    
    super.simulate(blacklistAnalyses);
    
    
    
    this.performanceValues = new HashMap<String, HashMap<String,Double>>();
    
    
    

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

    JSONObject performances;

    Iterator<String> iterator;
    String key;
    JSONObject performance;
    String reference;

    // Extract the result from "dcop" analysis
    performances = this.jsonObject.getJSONObject(PERFORMANCES_ID)
        .getJSONObject(DCOP_ANALYSIS_ID);
    iterator = performances.keys();

    if (!blacklistAnalyses.contains(DCOP_ANALYSIS_ID)) {

      RealResultsDatabase dcopResults = RealResultsDatabase
          .buildResultDatabase((NutmegRealPlot) plots.get(resultIdentifier++));

      while (iterator.hasNext()) {

        key = iterator.next();
        performance = performances.getJSONObject(key);

        if (performance.has(REFERENCE_ID)) {

          reference = performance.getString(REFERENCE_ID);
          
          if (dcopResults.isMember(reference)) {
            this.performanceValues.put(key,
                dcopResults.getRealValue(reference).getValue());
          } else {
            System.out.println("\"" + key + "\" not available in database");
          }
        }
      }

      try {
        this.performanceValues.put("A",
            this.session.getNumericValueAttribute("A").doubleValue());
      } catch (UnableToStartSession e) {
        e.printStackTrace();
        return null;
      }

    }

    // Extract the result from "dcmatch" analysis
    performances = this.jsonObject.getJSONObject(PERFORMANCES_ID)
        .getJSONObject(DCMATCH_ANALYSIS_ID);
    iterator = performances.keys();

    if (!blacklistAnalyses.contains(DCMATCH_ANALYSIS_ID)) {

      RealResultsDatabase dcmatchResults = RealResultsDatabase
          .buildResultDatabase((NutmegRealPlot) plots.get(resultIdentifier++));

      while (iterator.hasNext()) {

        key = iterator.next();
        performance = performances.getJSONObject(key);

        reference = performance.getString(REFERENCE_ID);

        if (dcmatchResults.isMember(reference)) {
          this.performanceValues.put(key,
              dcmatchResults.getRealValue(reference).getValue());
        } else {
          System.out.println("\"" + key + "\" not available in database");
        }
      }
    }

    // Extract the result from "stb" analysis
    if (!blacklistAnalyses.contains(STB_ANALYSIS_ID)) {

      NutmegComplexPlot plot = (NutmegComplexPlot) plots
          .get(resultIdentifier++);

      ComplexResultsDatabase stb = ComplexResultsDatabase
          .buildResultDatabase(plot);

      ComplexWaveform loopGain = stb.getComplexWaveform("loopGain");

      // System.err.println(loopGain);

      // waves.put("loopGain", loopGain);

      RealWaveform loopGainAbs = loopGain.abs().db20();

      RealWaveform loopGainPhase = loopGain.phaseDeg();

      // waves.put("loopGainAbs", loopGainAbs);
      // waves.put("loopGainPhase", loopGainPhase);

      RealValue a0 = loopGainAbs.getValue(loopGainAbs.xmin());
      RealValue ugbw = loopGainAbs.cross(0, 1);

      RealValue pm = loopGainPhase.getValue(ugbw.getValue());

      RealValue cof = loopGainPhase.cross(0, 1);

      RealValue gm = loopGainAbs.getValue(cof.getValue());

      this.performanceValues.put("a_0", a0.getValue());
      this.performanceValues.put("ugbw", ugbw.getValue());
      this.performanceValues.put("cof", cof.getValue());
      this.performanceValues.put("pm", pm.getValue());
      this.performanceValues.put("gm", gm.getValue());

    }

    // Extract the result from "tran" analysis
    if (!blacklistAnalyses.contains(TRAN_ANALYSIS_ID)) {

      RealResultsDatabase tran = RealResultsDatabase
          .buildResultDatabase((NutmegRealPlot) plots.get(resultIdentifier++));

      RealWaveform out = tran.getRealWaveform("OUT");

      RealWaveform rising = out.clip(100e-9, 50e-6);
      RealWaveform falling = out.clip(50.1e-6, 99.9e-6);

      double lower = 0.1 * this.getParameterValues().get("vs")
          - getParameterValues().get("vs") / 2;
      double upper = 0.9 * this.getParameterValues().get("vs")
          - getParameterValues().get("vs") / 2;

      RealValue point1 = rising.cross(lower, 1);
      RealValue point2 = rising.cross(upper, 1);

      this.performanceValues.put("sr_r",
          (upper - lower) / (point2.getValue() - point1.getValue()));

      if (this.performanceValues.get("sr_r") == Double.NaN) {
        this.performanceValues.put("sr_r", Double.MIN_VALUE);
      }

      point1 = falling.cross(upper, 1);
      point2 = falling.cross(lower, 1);

      this.performanceValues.put("sr_f",
          (lower - upper) / (point2.getValue() - point1.getValue()));

      if (this.performanceValues.get("sr_f") == Double.NaN) {
        this.performanceValues.put("sr_f", Double.MAX_VALUE);
      }

      this.performanceValues.put("overshoot_r", 100
          * (rising.ymax().getValue() - out.getValue(50e-6).getValue())
          / (out.getValue(50e-6).getValue() - out.getValue(100e-9).getValue()));

      if (this.performanceValues.get("overshoot_r") == Double.NaN) {
        this.performanceValues.put("overshoot_r", Double.MAX_VALUE);
      }

      this.performanceValues.put("overshoot_f", 100
          * (falling.ymin().getValue() - out.getValue(90e-6).getValue())
          / (out.getValue(90e-6).getValue() - out.getValue(50e-6).getValue()));

      if (this.performanceValues.get("overshoot_f") == Double.NaN) {
        this.performanceValues.put("overshoot_f", Double.MAX_VALUE);
      }

    }

    // Extract the result from "noise" analysis
    if (!blacklistAnalyses.contains(NOISE_ANALYSIS_ID)) {

      RealResultsDatabase noise = RealResultsDatabase
          .buildResultDatabase((NutmegRealPlot) plots.get(resultIdentifier++));

      RealWaveform out = noise.getRealWaveform("out");

      this.performanceValues.put("vn_1Hz", out.getValue(1).getValue());
      this.performanceValues.put("vn_10Hz", out.getValue(10).getValue());
      this.performanceValues.put("vn_100Hz", out.getValue(1e2).getValue());
      this.performanceValues.put("vn_1kHz", out.getValue(1e3).getValue());
      this.performanceValues.put("vn_10kHz", out.getValue(1e4).getValue());
      this.performanceValues.put("vn_100kHz", out.getValue(1e5).getValue());

    }

    // Extract the result from "dc1" analysis
    if (!blacklistAnalyses.contains(DC1_ANALYSIS_ID)) {

      RealResultsDatabase outswing = RealResultsDatabase
          .buildResultDatabase((NutmegRealPlot) plots.get(resultIdentifier++));

      RealWaveform out = outswing.getRealWaveform("OUT");
      RealWaveform out_ideal = outswing.getRealWaveform("OUT_IDEAL");
      out = out.subtract(out.getValue(0));

      RealWaveform rel_dev = out.subtract(out_ideal);

      rel_dev = rel_dev.abs().divide(this.getParameterValues().get("vsup"));

      RealWaveform rel_dev_lower = rel_dev.clip(rel_dev.xmin().getValue(), 0);
      RealWaveform rel_dev_upper = rel_dev.clip(0, rel_dev.xmax().getValue());

      RealValue vil = rel_dev_lower.cross(dev, 1);
      RealValue vih = rel_dev_upper.cross(dev, 1);

      RealValue voh = out.getValue(vih);
      RealValue vol = out.getValue(vil);

      this.performanceValues.put("v_ol",
          vol.getValue() + this.getParameterValues().get("vsup") / 2);
      this.performanceValues.put("v_oh",
          voh.getValue() + this.getParameterValues().get("vsup") / 2);

    } else {
      this.performanceValues.remove("v_ol");
      this.performanceValues.remove("v_oh");
    }

    // Extract the result from "xf" analysis
    if (!blacklistAnalyses.contains(XF_ANALYSIS_ID)) {

      ComplexResultsDatabase tf = ComplexResultsDatabase.buildResultDatabase(
          (NutmegComplexPlot) plots.get(resultIdentifier++));

      ComplexWaveform vsupp = tf.getComplexWaveform("VSUPP");
      ComplexWaveform vsupn = tf.getComplexWaveform("VSUPN");
      ComplexWaveform vid = tf.getComplexWaveform("VID");
      ComplexWaveform vicm = tf.getComplexWaveform("VICM");

      RealWaveform vsuppAbs = vsupp.abs().db20();
      RealWaveform vsupnAbs = vsupn.abs().db20();
      RealWaveform vidAbs = vid.abs().db20();
      RealWaveform vicmAbs = vicm.abs().db20();

      RealWaveform psrr_p = vidAbs.subtract(vsuppAbs);
      RealWaveform psrr_n = vidAbs.subtract(vsupnAbs);
      RealWaveform cmrr = vidAbs.subtract(vicmAbs);

      this.performanceValues.put("psrr_p",
          psrr_p.getValue(psrr_p.xmin()).getValue());
      this.performanceValues.put("psrr_n",
          psrr_n.getValue(psrr_n.xmin()).getValue());
      this.performanceValues.put("cmrr", cmrr.getValue(cmrr.xmin()).getValue());

    }

    // Extract the result from "ac" analysis
    if (!blacklistAnalyses.contains(AC_ANALYSIS_ID)) {

      ComplexResultsDatabase inswing = ComplexResultsDatabase
          .buildResultDatabase(
              (NutmegComplexPlot) plots.get(resultIdentifier++));

      RealWaveform out = inswing.getComplexWaveform("OUT").abs().db20();

      RealWaveform rel_dev_lower = out.clip(out.xmin().getValue(), 0);
      RealWaveform rel_dev_upper = out.clip(0, out.xmax().getValue());

      RealValue amp = out.getValue(0);
      RealValue vil = rel_dev_lower.cross(amp.getValue() - 3, 1);
      RealValue vih = rel_dev_upper.cross(amp.getValue() - 3, 1);

      this.performanceValues.put("v_il",
          vil.getValue() + this.getParameterValues().get("vsup") / 2);
      this.performanceValues.put("v_ih",
          vih.getValue() + this.getParameterValues().get("vsup") / 2);
    }

    // Extract the result from "dc3" analysis
    if (!blacklistAnalyses.contains(DC3_ANALYSIS_ID)) {

      RealResultsDatabase outshortl = RealResultsDatabase
          .buildResultDatabase((NutmegRealPlot) plots.get(resultIdentifier++));

      this.performanceValues.put("i_out_min",
          outshortl.getRealValue("DUT:O").getValue());

    }

    // Extract the result from "dc4" analysis
    if (!blacklistAnalyses.contains(DC4_ANALYSIS_ID)) {

      RealResultsDatabase outshorth = RealResultsDatabase
          .buildResultDatabase((NutmegRealPlot) plots.get(resultIdentifier++));

      this.performanceValues.put("i_out_max",
          outshorth.getRealValue("DUT:O").getValue());

    }

    return this;
  }
}