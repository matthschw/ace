package edlab.eda.ace;

import java.util.Random;

import org.json.JSONObject;

/**
 * The class {@link Parameter} is a handle that contains all information
 * regarding a parameter of the {@link AnalogCircuitEnvironment}.
 */
public final class Parameter {

  private final String name;
  private double min = Double.MIN_VALUE;
  private double max = Double.MAX_VALUE;
  private double grid = Double.NaN;
  private double init = 0;
  private boolean sizing = true;

  private Parameter(final String name, final double min, final double max,
      final double grid, final double init, final boolean sizing) {
    this.name = name;
    this.min = min;
    this.max = max;
    this.grid = grid;
    this.init = init;
    this.sizing = sizing;
  }

  /**
   * Create a {@link Parameter} from a JSON representation
   * 
   * @param name    Name of the parameter
   * @param jsonObj JSON object that contains all infomation
   * 
   * @return parameter
   */
  static Parameter get(final String name, final JSONObject jsonObj) {

    double min = Double.MIN_VALUE;

    try {
      min = jsonObj.getDouble("min");
    } catch (final Exception e) {
    }

    double max = Double.MAX_VALUE;

    try {
      max = jsonObj.getDouble("max");
    } catch (final Exception e) {
    }

    double grid = Double.NaN;

    try {
      grid = jsonObj.getDouble("grid");
    } catch (final Exception e) {
    }

    double init = 0;

    try {
      init = jsonObj.getDouble("init");

    } catch (final Exception e) {

      System.err.print(
          "No intial value is provided for \"" + name + "\", use 0 instead");
    }

    boolean sizing = false;

    try {

      sizing = jsonObj.getBoolean("sizing");

    } catch (final Exception e) {
    }

    return new Parameter(name, min, max, grid, init, sizing);
  }

  /**
   * Get the name of the parameter
   * 
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Get the lower bound of the parameter. The methods returns
   * <code>Double.MIN_VALUE</code> when no lower bound is initially defined.
   * 
   * @return lower bound
   */
  public double getMin() {
    return this.min;
  }

  /**
   * Get the upper bound of the parameter. The methods returns
   * <code>Double.MAX_VALUE</code> when no upper bound is initially defined.
   * 
   * @return upper bound
   */
  public double getMax() {
    return this.max;
  }

  /**
   * Get the grid of the parameter. All possible parameter values must be
   * <code>min + n*grid</code>, with the natural number <code>n&gt;=0</code>.
   * The methods returns <code>Double.NaN</code> when no grid is initially
   * defined.
   * 
   * @return grid
   */
  public double getGrid() {
    return this.grid;
  }

  /**
   * Get the initial value of the parameter.
   * 
   * @return initial value
   */
  public double getInit() {
    return this.init;
  }

  /**
   * The method returns if this parameter is a sizing parameter. A sizing
   * parameter is a parameter that is utilized to tweak the dimensions of
   * devices in an analog circuit. A parameter that is not a sizing parameter is
   * utilized to define the environment (supply voltage, biasing,etc.).
   * 
   * @return <code>true</code> when the parameter is a sizing parameter,
   *         <code>false</code> otherwise
   */
  public boolean isSizingParameter() {
    return this.sizing;
  }

  /**
   * The function returns a valid value for this parameter. When the value
   * exceeds <code>min</code> or <code>max</code> the closest value that is in
   * range is returned. When the parameter has a grid, the provided parameter is
   * pushed on the grid. When the parameter is not a sizing parameter, no
   * changes are performed.
   * 
   * @param value Value to be used
   * @return value that is valid
   * @see #getGrid()
   * @see #isSizingParameter()
   */
  public double getValidValue(double value) {

    if (this.isSizingParameter()) {

      if (value < this.min) {
        value = this.min;
      }

      if (value > this.max) {
        value = this.max;
      }

      if (this.grid != Double.NaN) {
        value = (Math.round((value - this.min) / this.grid) * this.grid)
            + this.min;
      }

      if (value > this.max) {
        value = this.max;
      }

      if (value < this.min) {
        value = this.min;
      }
    }

    return value;
  }

  /**
   * Get a random value of the parameter
   * 
   * @return random value
   */
  public double getRandom() {

    if (this.sizing) {

      final Random r = new Random();

      if (this.grid == Double.NaN) {

        return this.min + ((this.max - this.min) * r.nextDouble());

      } else if (this.max > this.min) {

        final int i = r
            .nextInt((int) Math.round((this.max - this.min) / this.grid));

        return this.min + (this.grid * i);

      } else {
        return this.min;
      }

    } else {
      return Double.NaN;
    }
  }
}