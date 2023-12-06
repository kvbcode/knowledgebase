package com.cyber.knowledgebase.fts.mapping.impl;

import com.cyber.knowledgebase.fts.mapping.DocumentParser;

import java.net.URI;
import java.util.List;

public abstract class AbstractFileParser implements DocumentParser {

    @Override
    public boolean isSupported(URI location) {
        String filename = getFilenameFromURI(location).toLowerCase();
        for (String extension : getSupportedFileExtensions()) {
            if (filename.endsWith(extension)) return true;
        }
        return false;
    }

    abstract protected List<String> getSupportedFileExtensions();

    protected String getFilenameFromURI(URI location) {
        String[] parts = location.getPath().split("/");
        String filename = parts[parts.length - 1];
        return filename;
    }
}
