package com.eactive.resourcehub.common.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 파일 확장자가 아닌 실제 파일 시그니처(magic bytes)를 검증한다.
 * 확장자를 바꾼 위장 파일 업로드를 방지하기 위해 사용된다.
 */
public final class FileMagicValidator {

    private FileMagicValidator() {}

    private static final byte[] SIG_PDF     = {0x25, 0x50, 0x44, 0x46};
    private static final byte[] SIG_JPG     = {(byte)0xFF, (byte)0xD8, (byte)0xFF};
    private static final byte[] SIG_PNG     = {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] SIG_ZIP     = {0x50, 0x4B, 0x03, 0x04};
    private static final byte[] SIG_OLE2    = {(byte)0xD0, (byte)0xCF, 0x11, (byte)0xE0,
                                                (byte)0xA1, (byte)0xB1, 0x1A, (byte)0xE1};
    private static final byte[] SIG_HWP_OLD = {0x48, 0x57, 0x50, 0x20, 0x44, 0x6F, 0x63}; // "HWP Doc"

    private static final Map<String, byte[][]> SIGNATURES = Map.of(
        "pdf",  new byte[][] {SIG_PDF},
        "jpg",  new byte[][] {SIG_JPG},
        "jpeg", new byte[][] {SIG_JPG},
        "png",  new byte[][] {SIG_PNG},
        "docx", new byte[][] {SIG_ZIP},
        "pptx", new byte[][] {SIG_ZIP},
        "hwpx", new byte[][] {SIG_ZIP},
        "hwp",  new byte[][] {SIG_OLE2, SIG_HWP_OLD},
        "ppt",  new byte[][] {SIG_OLE2}
    );

    /**
     * 파일의 처음 몇 바이트가 extension에 해당하는 시그니처와 일치하는지 확인한다.
     *
     * @return 시그니처가 일치하면 true, 알 수 없는 확장자거나 불일치하면 false
     */
    public static boolean validate(MultipartFile file, String extension) throws IOException {
        byte[][] candidates = SIGNATURES.get(extension.toLowerCase());
        if (candidates == null) return false;

        int maxSigLen = 0;
        for (byte[] sig : candidates) maxSigLen = Math.max(maxSigLen, sig.length);

        byte[] header = new byte[Math.max(maxSigLen, 4)]; // 최소 4바이트 읽어야 read < 4 조건이 소형 파일만 차단
        try (InputStream is = file.getInputStream()) {
            int read = is.read(header);
            if (read < 4) return false;
            if (read < header.length) {
                byte[] trimmed = new byte[read];
                System.arraycopy(header, 0, trimmed, 0, read);
                header = trimmed;
            }
        }

        for (byte[] sig : candidates) {
            if (startsWith(header, sig)) return true;
        }
        return false;
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return false;
        }
        return true;
    }
}
