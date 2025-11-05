package com.kele.pacs;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Github Copilot
 * @since 2025-11-05
 * DICOM存储服务
 * 实现了C-STORE SCP（Service Class Provider）功能，用于接收和存储DICOM图像
 */
public class DicomStorageService extends BasicCStoreSCP {

    private static final Logger LOG = LoggerFactory.getLogger(DicomStorageService.class);
    private final File storageDir;

    /**
     * 构造函数
     *
     * @param storageDir 存储目录
     */
    public DicomStorageService(File storageDir) {
        this.storageDir = storageDir;
        // 确保存储目录存在
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw new RuntimeException("Failed to create storage directory: " + storageDir);
        }
    }

    /**
     * C-STORE服务的核心方法，处理传入的DICOM数据
     *
     * @param as          与客户端的关联
     * @param pc          表示上下文
     * @param rq          请求的属性
     * @param dataStream  包含DICOM数据的流
     * @param rsp         响应的属性
     * @throws DicomServiceException 如果处理过程中发生错误
     */
    @Override
    protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream dataStream, Attributes rsp) throws DicomServiceException {
        String sopInstanceUID = rq.getString(Tag.SOPInstanceUID);
        String sopClassUID = rq.getString(Tag.SOPClassUID);
        File file = new File(storageDir, sopInstanceUID);

        LOG.info("Receiving DICOM object: SOPInstanceUID={}, SOPClassUID={}, from AE Title={}",
                sopInstanceUID, sopClassUID, as.getCallingAET());

        try (DicomOutputStream dos = new DicomOutputStream(file);
             DicomInputStream dis = new DicomInputStream(dataStream)) {
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.YES);
            Attributes dataset = dis.readDataset(-1, -1);
            dos.writeDataset(dis.getFileMetaInformation(), dataset);
            LOG.info("Successfully stored DICOM object to: {}", file.getAbsolutePath());
            rsp.setInt(Tag.Status, VR.US, Status.Success);
        } catch (IOException e) {
            LOG.error("Failed to store DICOM object: SOPInstanceUID={}, SOPClassUID={}", sopInstanceUID, sopClassUID, e);
            rsp.setInt(Tag.Status, VR.US, Status.ProcessingFailure);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
    }
}
