package com.aegis.agent.integration;

import org.springframework.core.io.ByteArrayResource;

public class ByteArrayResourceWithFilename extends ByteArrayResource {

    private final String fileName;

    public ByteArrayResourceWithFilename(byte[] byteArray, String fileName) {
        super(byteArray);
        this.fileName = fileName;
    }

    @Override
    public String getFilename() {
        return fileName;
    }
}
