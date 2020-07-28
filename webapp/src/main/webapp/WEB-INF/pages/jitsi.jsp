<%@ page contentType="text/html;charset=UTF-8" language="java"%>

<%
  String room = (String) request.getAttribute("room");
  String domain = (String) request.getAttribute("domain");
  String name = (String) request.getAttribute("name");
  String email = (String) request.getAttribute("email");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Jitsi Meet</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<script src='https://<%=domain%>/external_api.js'></script>
<script src="js/jitsi.js"></script>
<link rel="stylesheet" type="text/css" href="skin/jitsi.css">
</head>
<body>
  <div class="JitsiMeetContainer">
    <div id="container"></div>
    <script>
          init('<%=domain%>', '<%=room%>', '<%=name%>', '<%=email%>');
    </script>
  </div>
</body>
</html>