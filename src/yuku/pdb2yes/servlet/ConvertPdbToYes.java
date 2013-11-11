package yuku.pdb2yes.servlet;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import yuku.alkitab.yes2.io.MemoryRandomOutputStream;
import yuku.alkitabconverter.yes_common.Yes2Common;
import yuku.alkitabconverter.yet.YetFileOutput;
import yuku.pdb2yes.core.PDBMemoryStream;
import yuku.pdb2yes.core.PdbInput;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.logging.Logger;


public class ConvertPdbToYes extends HttpServlet {
    private static final Logger log = Logger.getLogger(ConvertPdbToYes.class.getName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println("Hello world from " + ConvertPdbToYes.class.getSimpleName());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final ServletFileUpload upload = new ServletFileUpload();
        final PrintWriter writer = resp.getWriter();
        resp.setContentType("text/html");
        writer.println("<pre>");

        try {
            final FileItemIterator iterator = upload.getItemIterator(req);
            PDBMemoryStream pdbMemoryStream = null;
            String pdbFilename = null;
            String outputformat = null;

            while (iterator.hasNext()) {
                final FileItemStream item = iterator.next();
                final String fieldName = item.getFieldName();

                if (item.isFormField()) {
                    log.info("Got a form field: " + fieldName);
                    if ("outputformat".equals(fieldName)) {
                        outputformat = Streams.asString(item.openStream());
                    }
                } else {
                    log.info("Got an uploaded file: " + fieldName + ", name = " + item.getName());

                    // You now have the filename (item.getName() and the
                    // contents (which you can read from stream). Here we just
                    // print them back out to the servlet output stream, but you
                    // will probably want to do something more interesting (for
                    // example, wrap them in a Blob and commit them to the
                    // datastore).
                    if ("pdbfile".equals(fieldName)) {
                        pdbFilename = item.getName();
                        pdbMemoryStream = new PDBMemoryStream(item.openStream(), pdbFilename);
                    }
                }
            }

            // input validations

            if (pdbMemoryStream == null) {
                writer.print("No pdbfile");
                return;
            }

            if (!"yes".equals(outputformat) && !"yet".equals(outputformat)) {
                writer.print("Unknown outputformat: " + outputformat);
                return;
            }

            // read pdb
            final PdbInput pdbInput = new PdbInput();
            final PdbInput.Params params = new PdbInput.Params();
            params.includeAddlTitle = true;
            params.inputEncoding = "utf-8";
            final PdbInput.Result result = pdbInput.read(pdbMemoryStream, params);

            writer.println("Result: " + result.textDb.getBookCount() + " books");

            final MemoryRandomOutputStream memory = new MemoryRandomOutputStream();
            final String outputfilename;

            // convert to outputformat
            if ("yes".equals(outputformat)) {
                Yes2Common.createYesFile(memory, result.versionInfo, result.textDb, result.pericopeData, true, null, null);
                outputfilename = pdbFilename == null ? "noname.yes" : pdbFilename.toLowerCase().endsWith(".pdb") ? pdbFilename.replaceAll("(?i)\\.pdb", ".yes") : (pdbFilename + ".yes");
            } else if ("yet".equals(outputformat)) {
                final YetFileOutput yet = new YetFileOutput(memory);
                yet.setVersionInfo(result.versionInfo);
                yet.setTextDb(result.textDb);
                yet.setPericopeData(result.pericopeData);
                yet.setXrefDb(null);
                yet.setFootnoteDb(null);
                yet.write();
                outputfilename = pdbFilename == null ? "noname.yet" : pdbFilename.toLowerCase().endsWith(".pdb") ? pdbFilename.replaceAll("(?i)\\.pdb", ".yet") : (pdbFilename + ".yet");
            } else {
                return;
            }


            writer.println("yes file length: " + memory.getBufferLength());

            // store to appengine blobstore
            // Get a file service
            FileService fileService = FileServiceFactory.getFileService();

            // Create a new Blob file with mime-type "text/plain"
            AppEngineFile file = fileService.createNewBlobFile("application/octet-stream", outputfilename);

            // Open a channel to write to it
            FileWriteChannel writeChannel = fileService.openWriteChannel(file, true);

            // Write to the channel
            writeChannel.write(ByteBuffer.wrap(memory.getBuffer(), memory.getBufferOffset(), memory.getBufferLength()));

            // Close and save the file path for writing later
            writeChannel.closeFinally();

            final BlobKey blobKey = fileService.getBlobKey(file);
            writer.println("Download yes file: <a href='/" + DownloadBlob.class.getName() + "?blobkey=" + blobKey.getKeyString() + "'>" + outputfilename + "</a>");
        } catch (PdbInput.InputException e) {
            writer.println("Error in input: " + e.getMessage());
        } catch (FileUploadException e) {
            throw new ServletException(e);
        }
    }
}
