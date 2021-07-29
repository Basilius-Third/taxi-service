<%@ page contentType="text/html;charset=UTF-8" %>
<style>
  <%@include file='/WEB-INF/views/css/table_dark.css' %>
</style>
<html>
<head>
  <title>Login</title>
</head>
<body>
<form method="post" id="login_page" action="${pageContext.request.contextPath}/login"></form>
<h1 class="table_dark">Login page</h1>
<h3 class="table_dark" style="color:red">${errorMsg}</h3>
<table border="1" class="table_dark">
  <tr>
    <th>Enter login</th>
    <th>Enter password</th>
    <th>Submit</th>
  </tr>
  <tr>
    <td>
      <input type="text" name="login" form="login_page" required>
    </td>
    <td>
      <input type="text" name="password" form="login_page" required>
    </td>
    <td>
      <input type="submit" name="submit" form="login_page" required>
    </td>
  </tr>
</table>
</body>
</html>
