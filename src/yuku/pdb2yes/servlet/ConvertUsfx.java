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
import yuku.alkitabconverter.usfx_common.UsfxToYet;
import yuku.pdb2yes.core.MemoryStream;
import yuku.pdb2yes.core.PdbInput;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Logger;


public class ConvertUsfx extends HttpServlet {
    private static final Logger log = Logger.getLogger(ConvertUsfx.class.getName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println("Hello world from " + ConvertUsfx.class.getSimpleName());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final ServletFileUpload upload = new ServletFileUpload();
        final PrintWriter writer = resp.getWriter();
        resp.setContentType("text/html");
        writer.println("<pre>");

        try {
            final FileItemIterator iterator = upload.getItemIterator(req);

	        String info_long_name = null;
	        String info_short_name = null;
	        String info_description = null;
	        String info_locale = null;

	        String book_name = null;
	        String book_abbr = null;
	        int book_0 = -1;

            String usfxFilename = null;
            String outputformat = null;

	        MemoryStream inputfilecontents = null;

            while (iterator.hasNext()) {
                final FileItemStream item = iterator.next();
                final String fieldName = item.getFieldName();

                if (item.isFormField()) {
                    log.info("Got a form field: " + fieldName);
                    if ("outputformat".equals(fieldName)) {
                        outputformat = Streams.asString(item.openStream());
                    }
                    if ("info_long_name".equals(fieldName)) {
                        info_long_name = Streams.asString(item.openStream());
                    }
                    if ("info_short_name".equals(fieldName)) {
                        info_short_name = Streams.asString(item.openStream());
                    }
                    if ("info_description".equals(fieldName)) {
                        info_description = Streams.asString(item.openStream());
                    }
                    if ("info_locale".equals(fieldName)) {
                        info_locale = Streams.asString(item.openStream());
                    }
                    if ("book_0".equals(fieldName)) {
                        book_0 = Integer.parseInt(Streams.asString(item.openStream()));
                    }
                    if ("book_name".equals(fieldName)) {
                        book_name = Streams.asString(item.openStream());
                    }
                    if ("book_abbr".equals(fieldName)) {
                        book_abbr = Streams.asString(item.openStream());
                    }
                } else {
                    log.info("Got an uploaded file: " + fieldName + ", name = " + item.getName());

                    // You now have the filename (item.getName() and the
                    // contents (which you can read from stream). Here we just
                    // print them back out to the servlet output stream, but you
                    // will probably want to do something more interesting (for
                    // example, wrap them in a Blob and commit them to the
                    // datastore).
                    if ("inputfile".equals(fieldName)) {
                        usfxFilename = item.getName();
	                    inputfilecontents = new MemoryStream(item.openStream());
                    }
                }
            }

            // input validations

            if (inputfilecontents == null) {
                writer.print("No input file contents");
                return;
            }

            if (!"yet".equals(outputformat)) {
                writer.print("Unknown outputformat: " + outputformat);
                return;
            }

	        final MemoryRandomOutputStream memory = new MemoryRandomOutputStream();

	        // begin conversion
	        final UsfxToYet converter = new UsfxToYet();
	        converter.u(
		        new InputStream[] {inputfilecontents.asInputStream()},
		        new int[]{book_0},
		        info_locale,
		        info_short_name,
		        info_long_name,
		        info_description,
		        Arrays.asList(book_name),
		        Arrays.asList(book_abbr),
		        memory
	        );

	        String outputfilename = usfxFilename == null ? "noname.yet" : usfxFilename.toLowerCase().endsWith(".xml") ? usfxFilename.replaceAll("(?i)\\.xml", ".yet") : (usfxFilename + ".xml");

	        writer.println("output file size: " + memory.getBufferLength());

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
            writer.println("Download output file: <a href='/" + DownloadBlob.class.getName() + "?blobkey=" + blobKey.getKeyString() + "'>" + outputfilename + "</a>");

        } catch (PdbInput.InputException e) {
            writer.println("Error in input: " + e.getMessage());
        } catch (FileUploadException e) {
            throw new IOException(e);
        }
    }
}
