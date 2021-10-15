package edlab.eda.ace.bench;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edlab.eda.ace.AnalogCircuitEnvironment;
import edlab.eda.ace.SingleEndedOpampEnvironment;

public class ForceBugs {

  private static final int PARALLEL = 84;
  private static final int PARAM_SETS = 10000;

  public static void main(String[] args) {

    ExecutorService executor = Executors.newFixedThreadPool(PARALLEL);
  
    for (int i = 0; i < PARALLEL; i++) {
      
      final int val = i;
      executor.execute(() -> run(val));
    }

    try {
      executor.awaitTermination(1000, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    executor.shutdown();

  }

  private static void run(int thread) {
    AnalogCircuitEnvironment env = SingleEndedOpampEnvironment.get("/home/sim",
        "/home/schweikardt/github/ace/resource/xh035-3V3/op2/",
        new String[] {
            new File("/home/schweikardt/github/ace/resource/xh035-3V3/op2/",
                "./../pdk").getAbsoluteFile().toString() });

    Map<String, Double> parameters;

    for (int i = 0; i < PARAM_SETS; i++) {

     // System.out.println("Thread " + thread + "|" + i + "/" + PARAM_SETS);

      parameters = env.getRandomSizingParameters();
      env.set(parameters);

      env.simulate();

      // System.out.println("Step " + i + "|" +
      // env.getPerformanceValues().size()
      // + "/" + env.getPerformanceIdentifiers().size());

      for (Entry<String, Double> entry : env.getPerformanceValues()
          .entrySet()) {

        if (entry.getValue() == Double.NaN || entry.getValue() == null) {

          System.err.println(entry.getKey() + " =" + entry.getValue());

          Properties properties = new Properties();

          for (Entry<String, Double> entry2 : parameters.entrySet()) {
            properties.put(entry2.getKey(), entry2.getValue());
          }

          try {
            File file = File.createTempFile("bug_op2_xh035", "map",
                new File(""));
            properties.store(new FileOutputStream(file), null);
          } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        } else {
          // System.out.println(entry.getKey() + " =" + entry.getValue());

        }
      }
    }
  }
}
