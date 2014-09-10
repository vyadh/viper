Viper
=====

Viper is a log viewer designed for tailing log files of different formats.

## Features

* Realtime "tail -f" style updates
* Tail multiple log files concurrently
* Support for common java.util.logging XML and text format (more to come)
* Support for uninterrupted reading from log files that rotate without application impact
  * No file locks taken, even on Windows
* Highlighting of log entries based on severity
* Realtime filtering based on log item severity
* Realtime filtering using convenient text search (e.g. "12:45 warning user dave login") 
* Responsive user interface
* Viewing of log item details, including any exception stack traces
* Double-click opening of other log files in the same instance
* Drag-and-drop opening of log files
* Ability to temporarily hide new items using freeze button

## Screenshot

![Screenshot with one log file open](.github/screenshot.png?raw=true "Example")
