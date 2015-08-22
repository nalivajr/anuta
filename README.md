# Anuta
ANdroid Useful Tools Assembly

## Overview
Anuta is a set of utilities, which may help to write Android applications.

Anuta does not require any additional libraries to be downloaded. Just add it as a dependency to your project and it's ready to be used.

## Components
Right now Anutalibrary contains next tools:
* [View Injection](https://github.com/nalivajr/anuta/wiki/View-Injection)
* [Adapter Tools](https://github.com/nalivajr/anuta/wiki/Anuta-Adapter)
* [Simple ORM](https://github.com/nalivajr/anuta/wiki/Simple-ORM)

All Anuta's tools can be accessed using instances of util classes, which are inside Anuta class. 

## Samples
In source code you can find two modules: anuta and app. Module `anuta` contains all the code of library. It can be compiled to .aar and used in other applications. Module `app` contains sample activities, which can help to anderstand how to use Anuta.

## News
Version v0.2.0 released

1. The code was moved to by.nalivajr package.
2. Cursor-like logic, which helps to iterate elements in database
3. New cursor based adapter added
4. Async Entity Manager allows to access database in background thread and receive result in callback.
5. New adapters and also a number of utils methods for adapters creation

Version v0.3.0 released. Alice was renamed to Anuta
1. The component, for background task execution and optional UI notification was added.
2. Relation in ORM
3. QueryBuilder was extended
4. Entity Cursor can be configured to auto-requery updated data

