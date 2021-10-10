package edlab.eda.ace;

public class Test {

  public static void main(String[] args) {

    SingleEndedOpampEnvironment op = SingleEndedOpampEnvironment
        .get("/home/sim", "./resource/xh035-3v3/op2", new String[] {
            "/mnt/data/pdk/XKIT/xh035/cadence/v6_6/spectre/v6_6_2/mos" });
    
    
    op.simulate();
    
  }
}