package com.eactive.resourcehub.common.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorage {

    /**
     * @return storagePath (subPath/storedFileName) relative to base-dir
     */
    String store(MultipartFile file, String subPath, String storedFileName) throws IOException;

    InputStream load(String storagePath) throws IOException;

    void delete(String storagePath) throws IOException;
}
