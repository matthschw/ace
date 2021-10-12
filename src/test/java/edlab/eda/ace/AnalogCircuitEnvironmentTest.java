package edlab.eda.ace;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;

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

              AnalogCircuitEnvironment env = null;

              switch (jsonObj.getString("environment")) {

              case "SingleEndedOpampEnvironment":

                env = SingleEndedOpampEnvironment.get(
                    resourcesFile.getAbsoluteFile().toString(),
                    subDirectoryFile.getAbsoluteFile().toString(),
                    new String[] { jsonObj.getString("models") },
                    new String[] {});
                break;

              default:
                fail("No environment \"" + jsonObj.getString("environment")
                    + "\" is available\n");
                break;
              }

              if (env != null) {

                try {

                  // simulate the circuit once
                  env.simulate();

                  for (int i = 0; i < NUM_OF_TESTS; i++) {
                    env.set(env.getRandomSizingParameters());
                    env.simulate();
                  }

                  // stop the environment
                  env.stop();

                } catch (Exception e) {
                  System.err.println(e);
                  fail(e.getMessage());
                }

              } else {

                fail("Cannot create environment for \""
                    + subDirectoryFile.getAbsolutePath() + "\"");
              }

            } catch (Exception e) {
              e.printStackTrace();
              fail("Unable to read JSON \"" + jsonFile.getAbsolutePath()
                  + "\"\n\t" + e.getMessage());
            }

          }
        }
      }
    }
  }
}
