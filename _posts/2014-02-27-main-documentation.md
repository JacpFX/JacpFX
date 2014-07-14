---
layout: documentation
title:  "JacpFX Documentation"
date:   2014-02-27 21:27:27
---


This documentation pages gives you detailed informations about all parts of JacpFX; how to bootstrap a new JacpFX application and the general usage. You may want to read the [quick-start tutorial](http://) to start with a JacpFX project directly.
## What it is##
JacpFX is an UI application framework based on JavaFX, supporting developers to structure an application with loosely coupled, reusable components. It frees you from the pitfalls of traditional multi-threaded programming helping you to separate the task execution from UI changes in you client application. JacpFX focusing on following goals to deliver best developer- and user-experience:

* Simple structuring of loosely coupled UI components
* Simple communication between components through a message-bus
* Support of asynchronous processes to avoid blocking UIs
* Lightweight (size and memory footprint)

## General structure ##
JacpFX has, like any other UI application framework, a hierarchic component structure to create client applications.

![JacpFX Component structure](http://jacp.googlecode.com/svn/wiki/JACP_Overview_v1.png "JacpFX Component structure") 

A JacpFX application consists of following components:

* An **[ApplicationLauncher](/applauncher/)**, which contains any configurations to bootstrap the application as well as the application main method.
* A **[Workbench](/workbech/)**, this is the root Node of the client application. He basically contains any perspective constraints and some application specific configurations.
* At least one **[Perspective](/perspective/)** to define the basic layout of your view
* **[UI Components](/components/)**, to define the contents in a perspective
* **[UI Fragments](/fragments/)**, to define parts of your UI in a component, this allows you to seperate one component in more fine-grained parts
* **[Stateful/Stateless service Components](/services/)**, non UI service components for task execution and communication with external systems
 
