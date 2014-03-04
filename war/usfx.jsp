<%@ page import="java.io.IOException" %>
<%@ page import="java.nio.charset.Charset" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.SortedMap" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<body>

<h2>Convert a single-book USFX file</h2>

<h3>… to a YET (text) file</h3>
<form action="/yuku.pdb2yes.servlet.ConvertUsfx" method="post" enctype="multipart/form-data">
    Input file: <input type="file" name="inputfile" /><br/>
    Book number (starting from 0): <input type="number" name="book_0" value="39" /><br/>
    Book name: <input type="text" name="book_name" size="20" value="Test book" /><br/>
    Book abbr: <input type="text" name="book_abbr" size="4" value="Ts" /><br/>

    <hr/>
    Info - version long name: <input type="text" name="info_long_name" value="Test Version" /><br/>
    Info - version short name: <input type="text" name="info_short_name" value="TST" /><br/>
    Info - version description: <input type="text" name="info_description" value="This is a test version (c) 2099" /><br/>
    Info - locale: <input type="text" name="info_locale" value="en" /><br/>
    <input type="hidden" name="outputformat" value="yet" />
    <input type="submit" value="Convert" />
</form>

</body>
</html>
