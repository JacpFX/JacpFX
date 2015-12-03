---
layout: post
title:  JacpFX 2.1 release
date:   2015-12-03 21:27:27
---
# JacpFX 2.1 release #
After a longer period without any releases I am pleased to announce JacpFX 2.1. This release is fixing some issues (see below), adds some minor enhancements, and a lot of cleanups and optimizations under the hood. 

#### Some enhancements:
- You can now provide your own WorkBenchLayout
	- If you dislike the default Layout implementation with placeholders for ToolBars and so on you can implement your own (more mobile friendly ?)
	- Just implement the WorkbenchDecorator interface and register you implementation in the ApplicationLauncher by overriding the WorkbenchDecorator getWorkbenchDecorator() method. If you have issues with the new default decorator, the old one is still available as DefaultLegacyWorkbenchDecorator.class
- The JacpContext provides now the fullQualified name
- Fluent concurrency API provided by JacpContext (https://github.com/JacpFX/JacpFX/issues/26)
- Provide SPI for persisting events (https://github.com/JacpFX/JacpFX/issues/23)
	- implement the MessageLogger interface, create a file called org.jacpfx.api.message.MessageLogger in META-INF/service and add your implementation


#### Fixed issues:
- OnShow method not called on application start ( https://github.com/JacpFX/JacpFX/issues/33 )
	- the lifecycle was fixed so for perspectives now @PostConstruct -> @OnShow (if it’s the current visible) -> @OnHide (if it was hided) -> @Predestroy is valid
- Let ClassFinder work in Gradle (https://github.com/JacpFX/JacpFX/issues/32)
	- JacpFX should work with Gradle out-of-the-box
- Inaccurate exception (https://github.com/JacpFX/JacpFX/issues/31)
	- Exceptions while loading the FXML should be now more meaningful
- CSS Error parsing (https://github.com/JacpFX/JacpFX/issues/28)
- ToolBar button management fails on two perspectives when second is initially inactive (https://github.com/JacpFX/JacpFX/issues/11)
- https://github.com/JacpFX/JacpFX/issues/46
- https://github.com/JacpFX/JacpFX/issues/45
- https://github.com/JacpFX/JacpFX/issues/43
- https://github.com/JacpFX/JacpFX/issues/42
- and more….

