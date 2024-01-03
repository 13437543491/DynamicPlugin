package com.quick.resid

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ResHandler {

    private static final int DEFAULT_RES_ID = 0x7f
    private static final int NEW_RES_ID = 0x6f

    void processResId(File aaptApk, File sourceOutputDir, File symbolOutputDir) {
        replaceR(aaptApk)
        replaceR(sourceOutputDir)
        if (symbolOutputDir != null && symbolOutputDir.exists()) {
            replaceR(symbolOutputDir)
        }
    }

    private void replaceApkDir(File apkFile) {
        if (apkFile.isDirectory()) {
            for (File file : apkFile.listFiles()) {
                replaceApkDir(file);
            }
        } else {
            if (apkFile.getName().contains(".ap_")) {
                try {
                    replaceApk(apkFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void replaceR(File rFile) {
        if (rFile.isDirectory()) {
            File[] files = rFile.listFiles()
            if (files != null) {
                for (File file : files) {
                    replaceR(file)
                }
            }
            return
        }

        String fileName = rFile.getName()

        if (fileName.endsWith(".ap_")) {
            replaceApk(rFile)
            return
        }

        if (fileName.startsWith("R.")) {
            if (fileName.endsWith(".java") || fileName.endsWith(".txt")) {
                replaceRFile(rFile)
            }
        }
    }

    private static void replaceApk(File apk) {
        File newZip = new File(apk.getAbsolutePath() + ".tmp")

        ZipFile zipFile = null
        ZipOutputStream out = null

        try {
            out = new ZipOutputStream(new FileOutputStream(newZip))

            zipFile = new ZipFile(apk)
            Enumeration<? extends ZipEntry> entries = zipFile.entries()
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement()

                InputStream inputStream = zipFile.getInputStream(zipEntry)
                byte[] data = IOUtils.toByteArray(inputStream)
                inputStream.close()

                String name = zipEntry.getName()
                if (name == "resources.arsc") {
                    replaceARSC(data)
                } else if (name.endsWith(".xml")) {
                    replaceXml(data)
                }

                out.putNextEntry(new ZipEntry(name))
                out.write(data)
                out.closeEntry()
            }
            out.close()
            zipFile.close()

            apk.delete()
            newZip.renameTo(apk)
        } catch (IOException e) {
            e.printStackTrace()
        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close()
                }
            } catch (IOException e) {
                e.printStackTrace()
            }
            try {
                if (out != null) {
                    out.close()
                }
            } catch (IOException e) {
                e.printStackTrace()
            }
        }

    }

    private static void replaceARSC(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

        if (byteBuffer.getShort() != (short) 0x0002) {
            throw new RuntimeException()
        }

        short headSize = byteBuffer.getShort()
        byteBuffer.position(headSize)

        if (byteBuffer.getShort() != (short) 0x0001) {
            throw new RuntimeException()
        }

        byteBuffer.getShort()
        short stringPoolSize = byteBuffer.getShort()
        byteBuffer.position(headSize + stringPoolSize)

        if (byteBuffer.getShort() != (short) 0x0200) {
            throw new RuntimeException()
        }

        int resIdPosition = headSize + stringPoolSize + 8
        byteBuffer.position(resIdPosition)

        int oldId = byteBuffer.getInt()
        if (oldId == DEFAULT_RES_ID) {
            byteBuffer.position(resIdPosition)
            byteBuffer.putInt(NEW_RES_ID)
        }
    }

    private static void replaceXml(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

        int type = byteBuffer.getInt()
        if (type != 0x00080003) {
            throw new RuntimeException()
        }

        while (data.length - byteBuffer.position() >= 4) {
            type = byteBuffer.getInt()

            if (type == 0x01000008 || type == 0x02000008) {
                int position = byteBuffer.position() + 3
                byteBuffer.position(position)
                if ((int) byteBuffer.get() != DEFAULT_RES_ID) {
                    continue
                }

                byteBuffer.position(position)
                byteBuffer.put((byte) NEW_RES_ID)

                position++
                byteBuffer.position(position)
            }
        }
    }

    private static void replaceRFile(File rFile) {
        BufferedWriter writer = null
        BufferedReader reader = null
        try {
            File newFile = new File(rFile.getAbsolutePath() + ".tmp")
            FileUtils.deleteQuietly(newFile)
            newFile.createNewFile()

            Pattern pattern = Pattern.compile(String.format("0x%02x[0-9a-fA-F]{6}", DEFAULT_RES_ID))

            writer = new BufferedWriter(new FileWriter(newFile))
            reader = new BufferedReader(new FileReader(rFile))

            String line
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line)
                StringBuffer sb = new StringBuffer()
                while (matcher.find()) {
                    String id = matcher.group()
                    String newId = String.format("0x%02x%s", NEW_RES_ID, id.substring(4))
                    matcher.appendReplacement(sb, newId)
                }
                matcher.appendTail(sb)

                writer.write(sb.toString())
                writer.newLine()
            }

            writer.flush()
            writer.close()
            reader.close()

            FileUtils.deleteQuietly(rFile)
            newFile.renameTo(rFile)
        } catch (Exception e) {
            e.printStackTrace()
            throw new RuntimeException(e)
        } finally {
            IOUtils.closeQuietly(writer)
            IOUtils.closeQuietly(reader)
        }
    }
}