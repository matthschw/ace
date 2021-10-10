package edlab.eda.ace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import edlab.eda.ardb.RealResultsDatabase;
import edlab.eda.cadence.rc.spectre.SpectreFactory;
import edlab.eda.reader.nutmeg.NutmegPlot;
import edlab.eda.reader.nutmeg.NutmegRealPlot;

public class SingleEndedOpampEnvironment extends AnalogCircuitEnvironment {

  public SingleEndedOpampEnvironment(SpectreFactory factory,
      JSONObject jsonObject, String netlist, File[] includeDirs,
      Set<String> blacklistAnalyses) {

    this.session = factory.createSession();

    for (File file : includeDirs) {

      try {

        this.session.addIncludeDirectory(file);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }

    this.session.setNetlist(netlist);
    this.jsonObject = jsonObject;

    this.blacklistAnalyses = blacklistAnalyses;
  }

  public static SingleEndedOpampEnvironment get(String simDir,
      String circuitDir, String[] includeDirs) {
    return get(simDir, circuitDir, includeDirs, new String[] {});
  }

  public static SingleEndedOpampEnvironment get(String simDir,
      String circuitDir, String[] includeDirs, String[] blacklistAnalyses) {

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

    factory.setTimeout(15, TimeUnit.MINUTES);

    File circuitDirFile = new File(circuitDir);

    if (!(circuitDirFile.exists() && circuitDirFile.isDirectory()
        && circuitDirFile.canWrite())) {
      System.err.println("\"" + simDir + "\" is not a directory");
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

      System.err.println("Cannot read JSON \"" + jsonFile.toString() + "\"");
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

    Set<String> blacklistAnalysesSet = new HashSet<String>();

    for (int i = 0; i < blacklistAnalyses.length; i++) {
      blacklistAnalysesSet.add(blacklistAnalyses[i]);
    }

    return new SingleEndedOpampEnvironment(factory, jsonObj, netlist,
        includeDirFiles, blacklistAnalysesSet);
  }

  @Override
  public void simulate() {

    List<NutmegPlot> plots;

    if (this.blacklistAnalyses.isEmpty()) {
      plots = this.session.simulate();
    } else {
      plots = this.session.simulate(this.blacklistAnalyses);
    }

    int resultIdentifier = 0;

    if (!this.blacklistAnalyses.contains("dcop")) {

      RealResultsDatabase dcopResults = RealResultsDatabase
          .buildResultDatabase((NutmegRealPlot) plots.get(resultIdentifier++));

      JSONObject performances = this.jsonObject.getJSONObject("performances")
          .getJSONObject("dcop");

      Iterator<String> iterator = performances.keys();
      String key;

      JSONObject performance;
      String reference;

      while (iterator.hasNext()) {

        key = iterator.next();
        performance = performances.getJSONObject(key);

        reference = performance.getString("reference");

        System.out.println(
            key + "=" + dcopResults.getRealValue(reference).getValue());

      }
    }

    if (!this.blacklistAnalyses.contains("dcmatch")) {
      
      RealResultsDatabase dcmatchResults = RealResultsDatabase
          .buildResultDatabase((NutmegRealPlot) plots.get(resultIdentifier++));

   
    }

  }
}