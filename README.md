# Kotlin Manager (Kotman)

## Usage
```
kotman [command] [args]

kotman new [name] [package]       - Create new project
kotman cli                        - Create CLI for your project
kotman ver [version]              - Change project gradle version

kotman deps                       - Show project dependencies
kotman add                        - Add dependency to the project

kotman gen                        - Run project code generator. It will scan for "// generate:" comments

kotman dist                       - Create zip package that contains files without sources

-- CONFIGURATION --
kotman config                     - Read already set configuration
kotman config author [name]       - Set new author for future projects
kotman config version [version]   - Set new version for future projects
kotman config kotlinver [version] - Set new kotlin version for future projects
kotman config corover [version]   - Set new kotlin coroutines version
```