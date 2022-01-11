package edlab.eda.ace;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class EnvironmentPoolTest {

  public static final int ENVS = 15;

  @Test
  void test() {

    AnalogCircuitEnvironment env;

    EnvironmentPool pool = new EnvironmentPool();

    for (int i = 0; i < ENVS; i++) {

      env = SingleEndedOpampEnvironment.get("/home/sim/schweikardt/ace",
          "/home/schweikardt/github/ace/resource/gpdk180-1V8/op2/",
          new String[] {
              new File("/home/schweikardt/github/ace/resource/gpdk180-1V8/op2/",
                  "./../pdk").getAbsoluteFile().toString() });

      // add envs to pool
      pool.add(env);
    }

    // execute all envs in the pool
    pool.execute();

  }

}
