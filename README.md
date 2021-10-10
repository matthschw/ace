# AC²E

Analog Circuit Characterization Library

## Installation


Please install the following dependencies manually:

- [nutmeg-reader](https://github.com/electronics-and-drives/nutmeg-reader) 
- [cadence-remote-control](https://github.com/electronics-and-drives/cadence-remote-control) 
- [cadence-remote-control](https://github.com/electronics-and-drives/spectre-remote-control) 
- [analog-results-database](https://github.com/electronics-and-drives/analog-results-database) 
- [ace](https://github.com/matthschw/ace) 

Clone the corresponding repositories, enter the directory and execute

```bash
$ mvn install
```
## Dependencies

- Cadence Spectre 19.1 or newer
- Java 1.8 or newer

## Environments

- `SingleEndedOpampEnvironment`

## Backends

- [generic-5V](https://github.com/matthschw/ace-generic-5V)
- [generic-1V](https://github.com/matthschw/ace-generic-1V)
- [xh035-3V3](https://gitlab-forschung.reutlingen-university.de/eda/ace-xh035-3v3)

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