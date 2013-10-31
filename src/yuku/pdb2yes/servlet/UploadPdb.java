package yuku.pdb2yes.servlet;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import yuku.alkitab.yes2.io.MemoryRandomOutputStream;
import yuku.alkitabconverter.yes_common.Yes2Common;
import yuku.pdb2yes.core.PDBMemoryStream;
import yuku.pdb2yes.core.PdbInput;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;


public class UploadPdb extends HttpServlet {
    private static final Logger log = Logger.getLogger(UploadPdb.class.getName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println("Hello world from " + UploadPdb.class.getSimpleName());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final ServletFileUpload upload = new ServletFileUpload();
        final PrintWriter writer = resp.getWriter();
        resp.setContentType("text/plain");

        try {
            final FileItemIterator iterator = upload.getItemIterator(req);
            PDBMemoryStream pdbMemoryStream = null;

            while (iterator.hasNext()) {
                final FileItemStream item = iterator.next();
                if (item.isFormField()) {
                    log.info("Got a form field: " + item.getFieldName());
                } else {
                    log.info("Got an uploaded file: " + item.getFieldName() + ", name = " + item.getName());

                    // You now have the filename (item.getName() and the
                    // contents (which you can read from stream). Here we just
                    // print them back out to the servlet output stream, but you
                    // will probably want to do something more interesting (for
                    // example, wrap them in a Blob and commit them to the
                    // datastore).
                    if ("pdbfile".equals(item.getFieldName())) {
                        final String pdbFilename = item.getName();
                        pdbMemoryStream = new PDBMemoryStream(item.openStream(), pdbFilename);
                    }
                }
            }

            if (pdbMemoryStream == null) {
                writer.print("No pdbfile");
                return;
            }

            final PdbInput pdbInput = new PdbInput();
            final PdbInput.Params params = new PdbInput.Params();
            params.includeAddlTitle = true;
            params.inputEncoding = "utf-8";
            final PdbInput.Result result = pdbInput.read(pdbMemoryStream, params);

            writer.println("Result:");
            writer.println(result.textDb.getBookCount() + " books");

            final MemoryRandomOutputStream memory = new MemoryRandomOutputStream();
            Yes2Common.createYesFile(memory, result.versionInfo, result.textDb, result.pericopeData, true, null, null);
            memory.close();

            writer.println("yes file length: " + memory.getBufferLength());

        } catch (FileUploadException e) {
            throw new ServletException(e);
        }
    }
}
