#Mocker



## Basic usage

## Running with auto-reload

To run Mocker with auto-reload do the following:

1. Run enableCompileOnChange.sh which uses Gradle to compile the sources when a change occurs on those sources.
2. Run runMocker.sh This will run the application (which is configured using auto-reload so that when the sources are re-compiled, the changes are piked up by the class loader)

To stop the application and/or auto-compile issue ctrl-c in both terminal windows.