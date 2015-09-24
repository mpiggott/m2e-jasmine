<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
  "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <title>Jasmine Spec Runner</title>

  <link rel="shortcut icon" type="image/png" href="lib/jasmine-1.3.1/jasmine_favicon.png">
  <link rel="stylesheet" type="text/css" href="jasmine.css">
  <script type="text/javascript" src="jasmine.js"></script>
  <script type="text/javascript" src="jasmine-html.js"></script>

  <!-- include source files here... -->
<#list preloadSources as source>
  <script src="${source}" charset="UTF-8"></script>
</#list>
<#list sources as source>
  <script src="${source}" charset="UTF-8"></script>
</#list>

  <!-- include spec files here... -->
<#list specs as spec>
  <script src="${spec}" charset="UTF-8"></script>
</#list>

  <script type="text/javascript">
    (function() {
      var jasmineEnv = jasmine.getEnv();
      jasmineEnv.updateInterval = 1000;

      var htmlReporter = new jasmine.HtmlReporter();

      jasmineEnv.addReporter(htmlReporter);

      jasmineEnv.specFilter = function(spec) {
        return htmlReporter.specFilter(spec);
      };

      var currentWindowOnload = window.onload;

      window.onload = function() {
        if (currentWindowOnload) {
          currentWindowOnload();
        }
        execJasmine();
      };

      function execJasmine() {
        jasmineEnv.execute();
      }

    })();
  </script>

</head>

<body>
</body>
</html>
