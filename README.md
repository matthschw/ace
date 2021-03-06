# AC²E
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) 

Analog Circuit Characterization Library

## Installation

Please install the following dependencies manually:

- [nutmeg-reader](https://github.com/electronics-and-drives/nutmeg-reader) 
- [cadence-remote-control](https://github.com/electronics-and-drives/cadence-remote-control) 
- [spectre-remote-control](https://github.com/electronics-and-drives/spectre-remote-control) 
- [analog-results-database](https://github.com/electronics-and-drives/analog-results-database) 

Clone the corresponding repositories, 
```bash
$ git clone --recursive  <PATH>
```
enter the directory and execute

```bash
$ mvn install
```

The documentation can be created with 
```bash
mvn javadoc:javadoc
```
and accessed at  `./target/apidocs/index.html`.


## Dependencies

- [Cadence® Spectre®](https://www.cadence.com/ko_KR/home/tools/custom-ic-analog-rf-design/circuit-simulation/spectre-simulation-platform.html) 19.1 or newer
- [OpenJDK 1.8](https://openjdk.java.net/) or newer 
- [Apache Maven 3.6](https://maven.apache.org/) or newer

## Environments

- *Single-Ended Operational Amplifier*

  The environment `SingleEndedOpampEnvironment` is utilized to extract
  common performances of a single-ended operational amplifier.

- *Nand-Gate with 4 inputs*

  The environment `Nand4Environment` is utilized to extract
  the switching thresholds of a NAND gate with 4 inputs.

- *schmitt trigger*

  The environment `SchmittTriggerEnvironment` is utilized to extract
  the thresholds and propagation delays of a schmitt trigger.

## Circuits

| Circuit                                                                           | Description                                                 | Environment                       |
| :-------------------------------------------------------------------------------: | :---------------------------------------------------------: | :-------------------------------: |
|  [op1](https://raw.githubusercontent.com/matthschw/ace/main/figures/op1.png)      | miller opamp with N differential pair                       | `SingleEndedOpampEnvironment`     |
|  [op2](https://raw.githubusercontent.com/matthschw/ace/main/figures/op2.png)      | symmetrical opamp with N differential pair                  | `SingleEndedOpampEnvironment`     |
|  [op3](https://raw.githubusercontent.com/matthschw/ace/main/figures/op3.png)      | (un) symmetrical opamp with N differential pair             | `SingleEndedOpampEnvironment`     |
|  [op4](https://raw.githubusercontent.com/matthschw/ace/main/figures/op4.png)      | symmetrical opamp with N differential pair and cascodes     | `SingleEndedOpampEnvironment`     |
|  [op5](https://raw.githubusercontent.com/matthschw/ace/main/figures/op5.png)      | (un) symmetrical opamp with N differential pair and cascode | `SingleEndedOpampEnvironment`     |
|  [op6](https://raw.githubusercontent.com/matthschw/ace/main/figures/op6.png)      | miller opamp with N differential pair                       | `SingleEndedOpampEnvironment`     |
|  [op7](https://raw.githubusercontent.com/matthschw/ace/main/figures/op7.png)      | feed-foward opamp                                           | `SingleEndedOpampEnvironment`     |
|  [op8](https://raw.githubusercontent.com/matthschw/ace/main/figures/op8.png)      | folded-cascode                                              | `SingleEndedOpampEnvironment`     |
|  [op9](https://raw.githubusercontent.com/matthschw/ace/main/figures/op9.png)      | folded-cascode with wide-swing current mirror               | `SingleEndedOpampEnvironment`     |
|  [op10](https://raw.githubusercontent.com/matthschw/ace/main/figures/op10.png)    | rail-to rail folded-cascode                                 | `SingleEndedOpampEnvironment`     |
|  [op11](https://raw.githubusercontent.com/matthschw/ace/main/figures/op11.png)    | rail-to rail folded-cascode with wide-swing current mirror  | `SingleEndedOpampEnvironment`     |
|  [nand4](https://raw.githubusercontent.com/matthschw/ace/main/figures/nand4.png)  | NAND with 4 inputs                                          | `Nand4Environment`                |
|  [st1](https://raw.githubusercontent.com/matthschw/ace/main/figures/st1.png)      | schmitt trigger                                             | `SchmittTriggerEnvironment`       |

## Backends

Backends are provided in the following technologies (PDKs).
They are added as submodule in `./resources`.
Some of the here referenced backends are proprietary.

| Circuit                                                                           | [sky130-1V8](https://github.com/matthschw/ace-sky130-1V8)  | [xt018-1V8](https://gitlab-forschung.reutlingen-university.de/eda/ace-xt018-1v8)  | [xh018-1V8](https://gitlab-forschung.reutlingen-university.de/eda/ace-xh018-1v8)  | [gpdk180-1V8](https://github.com/matthschw/ace-sky130-1V8)  | [xh035-3V3](https://gitlab-forschung.reutlingen-university.de/eda/ace-xh035-3v3)  |
| :-------------------------------------------------------------------------------: | :--------------------------------------------------------: | :-------------------------------------------------------------------------------: | :-------------------------------------------------------------------------------: | :---------------------------------------------------------: | :-------------------------------------------------------------------------------: |
|  [op1](https://raw.githubusercontent.com/matthschw/ace/main/figures/op1.png)      | &#10007;                                                   | &#10007;                                                                          | &#10007;                                                                          | &#10003;                                                    | &#10003;                                                                          |
|  [op2](https://raw.githubusercontent.com/matthschw/ace/main/figures/op2.png)      | &#10003;                                                   | &#10003;                                                                          | &#10007;                                                                          | &#10003;                                                    | &#10003;                                                                          |
|  [op3](https://raw.githubusercontent.com/matthschw/ace/main/figures/op3.png)      | &#10003;                                                   | &#10003;                                                                          | &#10007;                                                                          | &#10003;                                                    | &#10003;                                                                          |
|  [op4](https://raw.githubusercontent.com/matthschw/ace/main/figures/op4.png)      | &#10003;                                                   | &#10003;                                                                          | &#10007;                                                                          | &#10003;                                                    | &#10003;                                                                          |
|  [op5](https://raw.githubusercontent.com/matthschw/ace/main/figures/op5.png)      | &#10003;                                                   | &#10003;                                                                          | &#10007;                                                                          | &#10003;                                                    | &#10003;                                                                          |
|  [op6](https://raw.githubusercontent.com/matthschw/ace/main/figures/op6.png)      | &#10007;                                                   | &#10003;                                                                          | &#10007;                                                                          | &#10003;                                                    | &#10003;                                                                          |
|  [op7](https://raw.githubusercontent.com/matthschw/ace/main/figures/op7.png)      | &#10007;                                                   | &#10007;                                                                          | &#10007;                                                                          | &#10007;                                                    | &#10007;                                                                          |
|  [op8](https://raw.githubusercontent.com/matthschw/ace/main/figures/op8.png)      | &#10007;                                                   | &#10003;                                                                          | &#10007;                                                                          | &#10003;                                                    | &#10003;                                                                          |
|  [op9](https://raw.githubusercontent.com/matthschw/ace/main/figures/op9.png)      | &#10007;                                                   | &#10003;                                                                          | &#10007;                                                                          | &#10003;                                                    | &#10003;                                                                          |
|  [op10](https://raw.githubusercontent.com/matthschw/ace/main/figures/op10.png)    | &#10007;                                                   | &#10007;                                                                          | &#10007;                                                                          | &#10007;                                                    | &#10007;                                                                          |
|  [op11](https://raw.githubusercontent.com/matthschw/ace/main/figures/op11.png)    | &#10007;                                                   | &#10007;                                                                          | &#10007;                                                                          | &#10007;                                                    | &#10007;                                                                          |
|  [nand4](https://raw.githubusercontent.com/matthschw/ace/main/figures/nand4.png)  | &#10003;                                                   | &#10003;                                                                          | &#10003;                                                                          | &#10003;                                                    | &#10003;                                                                          |
|  [st1](https://raw.githubusercontent.com/matthschw/ace/main/figures/st1.png)      | &#10003;                                                   | &#10003;                                                                          | &#10007;                                                                          | &#10003;                                                    | &#10003;                                                                          |

## ToDo

- Provide MWE
- Work on `SingleEndedOpampEnvironment`
  - Differential input capacitance
  - Single-Ended input capacitance 
  - Output Resistance
  - THD
  - Settling time
- Work on `DifferentialOpampEnvironment`  
- Work on `ComparatorEnvironment`  
- Work on `BandgapEnvironment`  

## License

Copyright (C) 2022, [Reutlingen University](https://www.reutlingen-university.de), [Electronics & Drives](https://www.electronics-and-drives.de/)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see 
[https://www.gnu.org/licenses/](https://www.gnu.org/licenses).
