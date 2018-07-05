package uk.co.novinet.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Service
public class SftpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SftpService.class);

    @Value("${sftpUsername}")
    private String sftpUsername;

    @Value("${sftpPassword}")
    private String sftpPassword;

    @Value("${sftpHost}")
    private String sftpHost;

    @Value("${sftpPort}")
    private Integer sftpPort;

    @Value("${sftpRootDirectory}")
    private String sftpRootDirectory;

    public void sendToSftpEndpoint(InputStream inputStream, String destinationPath, String filename) {
        LOGGER.info("Going to upload file to {}", destinationPath);

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;

        try {
            session = jsch.getSession(sftpUsername, sftpHost, sftpPort);
            session.setPassword(sftpPassword);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            createDestinationDirectoryIfNecessary(sftpRootDirectory + "/" + destinationPath, sftpChannel);

            sftpChannel.put(inputStream, sftpRootDirectory + "/" + destinationPath + "/" + filename);
        } catch (Exception e) {
            LOGGER.error("Unable to sftp file {} to {}", filename, destinationPath, e);
            throw new RuntimeException(e);
        } finally {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private void createDestinationDirectoryIfNecessary(String destinationPath, ChannelSftp sftpChannel) throws SftpException {
        LOGGER.info("Going to create directory {}", destinationPath);
        String[] folders = destinationPath.split("/");
        for (String folder : folders) {
            if (folder.length() > 0) {
                try {
                    sftpChannel.cd(folder);
                } catch (SftpException e) {
                    sftpChannel.mkdir(folder);
                    sftpChannel.cd(folder);
                }
            }
        }
    }

    public List<SftpDocument> getAllDocumentsForPath(String path) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;
        List<SftpDocument> sftpDocuments = new ArrayList<>();

        try {
            session = jsch.getSession(sftpUsername, sftpHost, sftpPort);
            session.setPassword(sftpPassword);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            path = sftpRootDirectory + "/" + path;

            Vector<ChannelSftp.LsEntry> memberDocuments = null;

            try {
                memberDocuments = sftpChannel.ls(path);
            } catch (SftpException e) {
                return sftpDocuments;
            }

            for (ChannelSftp.LsEntry documentLsEntry : memberDocuments) {
                if (!documentLsEntry.getAttrs().isDir()) {
                    sftpDocuments.add(new SftpDocument(
                            documentLsEntry.getFilename(), path + "/" + documentLsEntry.getFilename(),
                            Instant.ofEpochMilli(documentLsEntry.getAttrs().getMTime()),
                            documentLsEntry.getAttrs().getSize())
                    );
                }
            }

            return sftpDocuments;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }
}
