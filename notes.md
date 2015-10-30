## Chrome debugging

In network pane use -scheme:chrome-extension to hide extensions

## Friday 30th onwards todo

 - Fix routing, dont build paths via strings, use route builders

-  Fix duplication susbcriptions, can be passed down?

 - Move schema to cljx and validate on both server and client
 
 - Add IP schema 
 
 - Catch 403 on ajax, queue the dispatch event, do auth flow, pick up the event and replay after login

 - Use user ns instead of custom repl
 
 - Rename stupid long webhookproxyweb to just whp

 - Split dev dependencies in leinegen
 
 - Add CSRF, test cookie is httponly, persistent session store, make cookie secret part of config

 - Borrow NS arrangement from system project

 - Expose debugging of ratom via clj->js export window method

  
 
  
