# AC²E
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) 

Analog Circuit Characterization Library

## Installation

Please install the following dependencies manually:

- [nutmeg-reader](https://github.com/electronics-and-drives/nutmeg-reader) 
- [cadence-remote-control](https://github.com/electronics-and-drives/cadence-remote-control) 
- [spectre-remote-control](https://github.com/electronics-and-drives/spectre-remote-control) 
- [analog-results-database](https://github.com/electronics-and-drives/analog-results-database) 
- [ace](https://github.com/matthschw/ace) 

Clone the corresponding repositories, 
```bash
$ git clone --recursive  <PATH>
```
enter the directory and execute
```bash
$ mvn install
```

## Dependencies

- [Cadence® Spectre®](https://www.cadence.com/ko_KR/home/tools/custom-ic-analog-rf-design/circuit-simulation/spectre-simulation-platform.html) 19.1 or newer
- [OpenJDK](https://openjdk.java.net/) 1.8 or newer 
- [Apache Maven](https://maven.apache.org/) 3.6 or newer

## Environments

- *Single-Ended Operational Amplifier*

  The environment `SingleEndedOpampEnvironment` is utilized to extract
  common performances of a single-ended operational amplifier.

## Circuits

| Circuit                                                            | Description                                              | Environment                       |
| :----------------------------------------------------------------: | :------------------------------------------------------: | :-------------------------------: |
|  [op1](https://github.com/matthschw/ace/tree/main/figures/op1.png) | miller opamp with N differential pair                    | `SingleEndedOpampEnvironment`     |
|  [op2](https://github.com/matthschw/ace/tree/main/figures/op2.png) | symmetrical opamp with N differential pair               | `SingleEndedOpampEnvironment`     |
|  [op3](https://github.com/matthschw/ace/tree/main/figures/op3.png) | (un) symmetrical opamp with N differential pair          | `SingleEndedOpampEnvironment`     |
|  [op4](https://github.com/matthschw/ace/tree/main/figures/op4.png) | symmetrical opamp with N differential pair and cascodes  | `SingleEndedOpampEnvironment`     |
|  op5                                                               | N/A                                                      | N/A                                  |
|  [op6](https://github.com/matthschw/ace/tree/main/figures/op6.png) | miller opamp with N differential pair                    | `SingleEndedOpampEnvironment`     |

## Backends

Backends are provided in the following technologies (PDKs).
They are added as submodule in `./resources`.
Some of the here referenced backends are proprietary.

- [generic-5V](https://github.com/matthschw/ace-generic-5V)
  - [ ] op1
  - [x] op2
  - [ ] op3
  - [ ] op4
  - [ ] op6

- [generic-1V](https://github.com/matthschw/ace-generic-1V)
  - [ ] op1
  - [ ] op2
  - [ ] op3
  - [ ] op4
  - [ ] op6

- [xh035-3V3](https://gitlab-forschung.reutlingen-university.de/eda/ace-xh035-3v3)
  - [x] op1
  - [x] op2
  - [x] op3
  - [x] op4
  - [x] op6

## ToDo

- Provide MWE

## License

Copyright (C) 2021, [Electronics & Drives](https://www.electronics-and-drives.de/)

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