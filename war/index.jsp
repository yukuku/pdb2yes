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

<p>As of 2016, this online tool is no longer supported. <small>Reason: Google stopped supporting Blobstore and I haven't had time to 
learn how to use the new Google Storage option in Java.</small> Please use the offline tools available here: 
Tools for converting PalmBible+ PDB files to .yet files, and from .yet files to .yes files and internal app files, are available at <a href='https://goo.gl/3wo5zv'>https://goo.gl/3wo5zv</a>.
</p>

<p>
Run <tt>java -jar PdbToYet.jar</tt> (or other jar files) to see instructions how to use that.
Please write to help@bibleforandroid.com if you need help.
</p>

<!--
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
-->

</body>
</html>
