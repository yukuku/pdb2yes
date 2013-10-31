package yuku.pdb2yes.servlet;

import com.google.appengine.api.blobstore.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;


public class DownloadBlob extends HttpServlet {
    private static final Logger log = Logger.getLogger(DownloadBlob.class.getName());

    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String blobkey = req.getParameter("blobkey");
        if (blobkey == null) {
            resp.sendError(404, "Invalid blob key");
            return;
        }
        final BlobKey key = new BlobKey(blobkey);

        final BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(key);
        final String filename = blobInfo.getFilename();
        resp.setHeader("Content-Disposition", "attachment; filename=" + filename);

        blobstoreService.serve(key, resp);
    }
}
