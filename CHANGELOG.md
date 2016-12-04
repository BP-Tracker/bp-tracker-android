Change Log
==========
Changes are documented here.


[v0.0.3] - Dec 2, 2016
-------------------------
### Added
  - API for calling functions
  - BPT function classes
  - New permission: `com.bptracker.permission.RECEIVE_DEVICE_EVENTS` for broadcast receivers
  - Firmware datatype TestInput

### Upated
  - Function-calls content provider (added event_id and event_data columns)

### Removed
  - DataTypeException for IllegalArgumentException


[v0.0.2] - Nov 25, 2016
-------------------------
### Added
  - New event providers
  - Custom cloudsdk build for SSE events issue (https://github.com/spark/spark-sdk-android/issues/10)
  - Panic state notification
  - Use gradle v2.2.2
  - New permission: `com.bptracker.permission.RECEIVE_EVENTS`
  - Intent and firmware classes

### Upated
  - Major code refactoring

### Removed
  - AppCompat v7 support libraries
  - Unused themes/styles
  - Renamed provider class names


[v0.0.3]: https://github.com/BP-Tracker/bp-tracker-android/releases/tag/v0.0.3
[v0.0.2]: https://github.com/BP-Tracker/bp-tracker-android/releases/tag/v0.0.2
