<%@ page import="java.io.IOException" %>
<%@ page import="java.nio.charset.Charset" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.SortedMap" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
void dropdownCharset(JspWriter pw) throws IOException {
    final SortedMap<String,Charset> charsets = Charset.availableCharsets();
    pw.println("<select name='charset'>");
    for (Map.Entry<String, Charset> e : charsets.entrySet()) {
        final String name = e.getKey();
        final String displayName = e.getValue().displayName();
        if ("UTF-8".equals(name)) {
            pw.print(String.format("<option value='%s' selected >%s</option>%n", name, displayName));
        } else {
            pw.print(String.format("<option value='%s'>%s</option>%n", name, displayName));
        }
    }
    pw.println("</select>");
}
%>
<html>
<body>

<h2>Convert a PalmBible+ PDB file</h2>

<h3>… to a YES file</h3>
<form action="/yuku.pdb2yes.servlet.ConvertPdb" method="post" enctype="multipart/form-data">
    Input file: <input type="file" name="pdbfile" /><br/>
    PDB encoding: <% dropdownCharset(out); %><br/>
    <input type="hidden" name="outputformat" value="yes" />
    <input type="submit" value="Convert" />
</form>

<h3>… or to a YET (text) file</h3>
<form action="/yuku.pdb2yes.servlet.ConvertPdb" method="post" enctype="multipart/form-data">
    Input file: <input type="file" name="pdbfile" /><br/>
    PDB encoding: <% dropdownCharset(out); %><br/>
    <input type="hidden" name="outputformat" value="yet" />
    <input type="submit" value="Convert" />
</form>

</body>
</html>
