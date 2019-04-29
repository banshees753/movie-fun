package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private BlobStore blobStore;


    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) throws IOException {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        Blob blob = new Blob(Long.toString(albumId), uploadedFile.getInputStream(), uploadedFile.getContentType());
        blobStore.put(blob);
        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        Optional<Blob> blob = blobStore.get(Long.toString(albumId));
        HttpHeaders headers;
        byte[] imageBytes = getCoverImageBytes(albumId);

        if (blob.isPresent()) {
            String contentType = blob.get().contentType;
            headers = createImageHttpHeaders(contentType, imageBytes);

        } else {
            URL defaultCover = this.getClass().getClassLoader().getResource("default-cover.jpg");
            Path coverFilePath = Paths.get(defaultCover.toURI());
            String contentType = new Tika().detect(coverFilePath);
            headers = createImageHttpHeaders(contentType, imageBytes);
        }

        return new HttpEntity<>(imageBytes, headers);
    }

    private HttpHeaders createImageHttpHeaders(String contentType, byte[] imageBytes) throws IOException {


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    private byte[] getCoverImageBytes(long albumId) throws URISyntaxException, IOException {
        Optional<Blob> blob = blobStore.get(Long.toString(albumId));
        Path coverFilePath;

        if (blob.isPresent()) {
                byte[] bytes = IOUtils.toByteArray(blob.get().inputStream);
                return bytes;
        } else {
            URL defaultCover = this.getClass().getClassLoader().getResource("default-cover.jpg");
            coverFilePath = Paths.get(defaultCover.toURI());
        }

        return readAllBytes(coverFilePath);
    }
}
