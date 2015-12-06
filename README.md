# Anuta
ANdroid Useful Tools Assembly

![anuta_logo_128](https://cloud.githubusercontent.com/assets/5110943/9461786/9f41b9ec-4b19-11e5-88cf-c14af1c65d9d.png)

## Overview
Anuta is a set of utilities, which may help to write Android applications.

Anuta does not require any additional libraries to be downloaded. Just add it as a dependency to your project and it's ready to be used.

## Components
Right now Anutalibrary contains next tools:
* [View Injection](https://github.com/nalivajr/anuta/wiki/View-Injection)
* [Adapter Tools](https://github.com/nalivajr/anuta/wiki/Anuta-Adapter)
* [Simple ORM](https://github.com/nalivajr/anuta/wiki/Simple-ORM)
* [Background task execution](https://github.com/nalivajr/anuta/wiki/Background-task-executor)

All Anuta's tools can be accessed using instances of util classes, which are inside Anuta class. 

## Samples
In source code you can find two modules: anuta and app. Module `anuta` contains all the code of library. It can be compiled to .aar and used in other applications. Module `app` contains sample activities, which can help to anderstand how to use Anuta.

## Repository
### Maven
`<dependency>`
	`<groupId>com.github.nalivajr</groupId>`
	`<artifactId>anuta</artifactId>`
	`<version>1.0.1-beta</version>`
`</dependency>`

### Gradle
`compile 'com.github.nalivajr:anuta:1.0.1-beta'`

## News
#### Version v0.2.0 released

1. The code was moved to by.nalivajr package.
2. Cursor-like logic, which helps to iterate elements in database
3. New cursor based adapter added
4. Async Entity Manager allows to access database in background thread and receive result in callback.
5. New adapters and also a number of utils methods for adapters creation

#### Version v0.3.0 released.

1. Alice was renamed to Anuta
2. The component, for background task execution and optional UI notification was added.
3. Relation in ORM
4. QueryBuilder was extended
5. Entity Cursor can be configured to auto-requery updated data
6. Background task execution

#### Version v0.3.1 released.

1. Test for Anuta entity manager were added
2. MIME-types can be specified for entities
3. LazyCollection introduced to represent not loaded related items collections.
4. The ability to specify cascade insert/update/delete strategies for related entities
5. Cascade deletetion was added

#### Version v0.3.2 released.

1. Anuta is available from Maven repository
2. Improving performance for Anuta ORM
3. Extending availability for new Android versions
4. Fixing issues
