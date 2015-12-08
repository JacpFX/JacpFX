---
layout: post
title:  JacpFX 2.0.1 bugfix release
date:   2014-09-17 21:27:27
---
# JacpFX 2.0.1 bugfix release #
Today we released a bugfix release of JacpFX (2.0.2) which is solving mainly two bugs:
<br/>
1. When using JacpFX on Windows in Eclipse/Netbeans/… we fixed a bug finding the correct JacpFX components
<br/>
2. under some conditions a perspective view may be empty on perspective switch
<br/>
Beside the bugfixes we released also a „minimal launcher“, so you can create JacpFX applications without any other dependencies. With the Spring Launcher you always had an overhead even if you don’t want to use Spring. To use the new application launcher add the maven dependency:
<br/>

```xml
<dependency>
    <groupId>org.jacpfx</groupId>
    <artifactId>jacpfx.JavaFXLauncher</artifactId>
    <version>${jacp.version}</version>
    <scope>compile</scope>
</dependency>
```
<br/>
and add a launcher like this:
<br/>
<script src="https://gist.github.com/amoAHCP/4cb801f8c982bceadc03.js"></script>
