package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.String.format;
import static java.nio.file.Files.newInputStream;

@Component
public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        File targetFile = getCoverFile(Long.parseLong(blob.name));

        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(IOUtils.toByteArray(blob.inputStream));
        }

    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        try {
            Path coverFilePath = getExistingCoverPath(Long.parseLong(name));
            InputStream inputStream = newInputStream(coverFilePath);
            String contentType = new Tika().detect(coverFilePath);
            Blob blob = new Blob(name, inputStream, contentType);
            return Optional.of(blob);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void deleteAll() {

    }


    private File getCoverFile(long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }


    private Path getExistingCoverPath(long albumId) throws URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            URL defaultCover = this.getClass().getClassLoader().getResource("default-cover.jpg");
            coverFilePath = Paths.get(defaultCover.toURI());
        }

        return coverFilePath;
    }
}
