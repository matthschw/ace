package edlab.eda.ace;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.util.HashSet;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class AnalogCircuitEnvironmentTest {

  public static final int NUM_OF_TESTS = 10;

  @Test
  void test() {

    File resourcesFile = new File("resource");

    String[] directories = resourcesFile.list(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {
        return new File(current, name).isDirectory();
      }
    });

    File directoryFile;
    File subDirectoryFile;
    File jsonFile;
    String[] subDirectories;
    JSONObject jsonObj;

    for (String directory : directories) {

      directoryFile = new File(resourcesFile, directory);

      subDirectories = directoryFile.list(new FilenameFilter() {
        @Override
        public boolean accept(File current, String name) {
          return new File(current, name).isDirectory();
        }
      });

      for (String subDirectory : subDirectories) {

        subDirectoryFile = new File(directoryFile, subDirectory);

        if (subDirectoryFile.isDirectory()) {

          jsonFile = new File(subDirectoryFile,
              AnalogCircuitEnvironment.JSON_FILE_NAME);

          if (jsonFile.exists()) {

            try {

              jsonObj = new JSONObject(
                  new String(Files.readAllBytes(jsonFile.toPath())));

              jsonObj.has("ignore");

              if (jsonObj.has("ignore") && jsonObj.getBoolean("ignore")) {
                System.out.println("[INFO] Ignore test of "
                    + subDirectoryFile.getAbsoluteFile().toString() + " ...");
              } else {
                AnalogCircuitEnvironment env = null;

                System.out.println("[INFO] Run test of "
                    + subDirectoryFile.getAbsoluteFile().toString() + " ...");

                switch (jsonObj.getString("environment")) {

                case "SingleEndedOpampEnvironment":

                  env = SingleEndedOpampEnvironment.get("/tmp",
                      subDirectoryFile.getAbsoluteFile().toString(),
                      new String[] { new File(subDirectoryFile, "./../pdk")
                          .getAbsoluteFile().toString() });

                  break;

                case "Nand4Environment":

                  env = Nand4Environment.get("/tmp",
                      subDirectoryFile.getAbsoluteFile().toString(),
                      new String[] { new File(subDirectoryFile, "./../pdk")
                          .getAbsoluteFile().toString() });

                  break;
                case "SchmittTriggerEnvironment":

                  env = SchmittTriggerEnvironment.get("/tmp",
                      subDirectoryFile.getAbsoluteFile().toString(),
                      new String[] { new File(subDirectoryFile, "./../pdk")
                          .getAbsoluteFile().toString() });

                  break;

                default:
                  System.err.println("[INFO] No environment \""
                      + jsonObj.getString("environment") + "\" is available");
                  break;
                }

                if (env != null) {

                  try {

                    for (int i = 0; i < NUM_OF_TESTS; i++) {

                      if (i > 0) {
                        env.set(env.getRandomSizingParameters());
                      }

                      env.simulate(new HashSet<String>(), env.getCorners());

                      // check if result for all identifiers is available
                      /*
                       * for (String id : env.getPerformanceIdentifiers()) { if
                       * (env.getPerformanceValues().get(id) == null) {
                       * fail("Performance \"" + id + "\" missing"); } }
                       */
                    }

                    String file = env.saveStatus(true);

                    if (!env.setStatus(file)) {
                      fail("Unable to set status");
                    }

                  } catch (Exception e) {
                    fail("Unable to simulate\n" + e);
                  }
                }
              }

            } catch (Exception e) {
              fail("Unable to read JSON \"" + jsonFile.getAbsolutePath()
                  + "\"\n\t" + e.getMessage());
            }
          }
        }
      }
    }
  }
}