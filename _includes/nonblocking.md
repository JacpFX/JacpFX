
#JacpFX Non Blocking UI#
When an UI Component receives a message, it first executes a handle method inside a worker thread and then it executes a handle method on FX Appliaction Thread. A JacpFX component is similar to an Actor, it has a unique ID and a message box where all the messages are queued and it can send messages to itself or to other components.

